package system.applications.MS

import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import system.applications.Application
import system.applications.ApplicationManager
import system.applications.ExternalAppManager
import system.applications.MS.model.MSLegacyApplication
import system.applications.MS.model.MSUWPApplication
import system.applications.MS.model.exception.ApplicationCreateException
import system.startup.StartupManager
import system.storage.StorageManager
import utils.CaseInsensitiveMap
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView

class MSApplicationManager(storageManager: StorageManager, private val startupManager: StartupManager) : ApplicationManager(storageManager) {

    // This map will hold the applications, associated with their id
    private var applicationMap = CaseInsensitiveMap<Application>()

    private val isPowerShellEnabled: Boolean

    private var robot: Robot? = null  // Used for the key and alt tab workaround.

    private val linkCacheManager: LinkCacheManager = LinkCacheManager(storageManager)
    private val externalAppManager = ExternalAppManager(storageManager)

    // This variable will hold the PID of the dokey process.
    private val dokeyPID: Int

    init {
        // Initialize the link cache manager
        linkCacheManager.load()

        // Initialize the external app manager
        externalAppManager.load()

        // Check if powershell is enabled in this machine
        isPowerShellEnabled = checkPowerShellEnabled()

        disableForegroundLock()

        // Initialize the robot
        robot = Robot()
        robot!!.autoDelay = 5
        robot!!.isAutoWaitForIdle = true

        dokeyPID = startupManager.pid
    }

    /**
     * Disable foreground lock that can cause problems when opening an application
     */
    private fun disableForegroundLock() {
        // Change the foreground timeout
        //win32gui.SystemParametersInfo(win32con.SPI_SETFOREGROUNDLOCKTIMEOUT, 0, win32con.SPIF_SENDWININICHANGE | win32con.SPIF_UPDATEINIFILE)
        WUser32.INSTANCE.SystemParametersInfo(0x2001, 0, 0, 0x01 or 0x02)
    }

    /**
     * Focus an application if already open or start it if not.
     *
     * @param appId path to the application.
     * @param forceRun if the application is not running, start it.
     * @return true if succeeded, false otherwise.
     */
    @Synchronized
    fun openApplication(appId: String?, forceRun: Boolean): Boolean {
        if (appId == null)
            return false

        val hasBeenFocused = WinApplicationLib.INSTANCE.focusApplication(WString(appId))
        if (hasBeenFocused > 0) {  // Succeeded in focusing the app
            return true
        }else if (hasBeenFocused == -2) {  // the application was open, but could not be focused.
            return false
        }

        // Get the requested application and Make sure the app is valid before opening it
        val application = getApplicationOrAttemptToAddItIfNotExisting(appId) ?: return false

        // Try to open the application
        if (forceRun) {
            return application.open()
        }

        return false
    }

    /**
     * Focus an application if already open or start it if not.
     *
     * @param applicationId path to the application.
     * @return true if succeeded, false otherwise.
     */
    @Synchronized
    override fun openApplication(applicationId: String): Boolean {
        return openApplication(applicationId, true)
    }

    /**
     * Return the icon file associated with the specified application.
     * @param applicationId path to the application
     * @return the icon image File object.
     */
    override fun getApplicationIcon(applicationId: String): File? {
        // Get the application
        var application = getApplicationOrAttemptToAddItIfNotExisting(applicationId)

        // Make sure the application exists
        return if (application != null) {
            File(application.iconPath!!)
        } else {
            null
        }
    }

    override fun open(filePath: String): Boolean {
        // Make sure it exists
        val file = File(filePath)
        if (!file.isDirectory && !file.isFile) {
            return false
        }

        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("explorer", filePath))

            //            // Also focus the explorer.exe application to bring it to front.
            //            if (explorerApp != null) {
            //                openApplication(explorerApp.getExecutablePath(), false);
            //            }

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    override fun openWebLink(url: String): Boolean {
        // Make sure the url is valid
        try {
            val u = URL(url)
            u.toURI()
        } catch (e: Exception) {
            LOG.fine("URL CONVERSION ERROR: " + url + " " + e.toString())
            return false
        }

        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            // Add the ending space as a workaround because if the url has parameters, explorer interprets it wrong
            // unless there are quotes and the space force the quotes to appear.
            val proc = runtime.exec(arrayOf("explorer", "$url "))
            proc.waitFor()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return false
    }

    override fun openTerminalWithCommand(command: String): Boolean {
        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            runtime.exec(arrayOf("cmd", "/c", "start", "cmd.exe", "/k", command))

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    override fun focusDokey(): Boolean {
        // If Dokey is already focused, return immediately
        return if (activePID == startupManager.pid) {
            false
        } else {
            // Focus dokey by opening the currently active Dokey process
            openApplication(startupManager.currentExecutablePath, false)
        }
    }

    override fun focusSearch(): Boolean {
        val dokeyHwnd = User32.INSTANCE.FindWindow(null, "Dokey Search")
        val foregroundHwnd = User32.INSTANCE.GetForegroundWindow()

        // Check if dokey is already the foreground process
        if (dokeyHwnd.pointer == foregroundHwnd.pointer) {
            return false
        }

        // Search bar wasn't focused, the workaround is simple
        // Get the search bar window rect, and then simulate a click on it
        // to focus the window.

        // Get the dokey window rect
        val dokeyRect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(dokeyHwnd, dokeyRect)

        // Get the middle coordinate
        val clickX = (dokeyRect.left + dokeyRect.right) / 2
        val clickY = (dokeyRect.top + dokeyRect.bottom) / 2

        if (robot == null)
            return false

        // Get the current mouse position
        val mousePoint = WinDef.POINT()
        User32.INSTANCE.GetCursorPos(mousePoint)

        // Simulate the click
        robot!!.mouseMove(clickX, clickY)
        robot!!.mousePress(InputEvent.BUTTON1_MASK)
        robot!!.mouseRelease(InputEvent.BUTTON1_MASK)

        // Return to the previous position
        robot!!.mouseMove(mousePoint.x, mousePoint.y)

        return true
    }

    /**
     * Get the application associated with the given executable path.
     * @param applicationId the path to the application.
     * @return the Application associated with the executable path if found, null otherwise.
     */
    override fun getApplication(applicationId: String): Application? {
        return applicationMap[applicationId]
    }

    /**
     * @return PID of the current active system.window. -1 is returned in case of errors.
     */
    @Synchronized
    override fun getActivePID(): Int {
        val hwnd = User32.INSTANCE.GetForegroundWindow()

        // Get the PID
        val PID = IntByReference()
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, PID)

        return PID.value
    }

    private fun getApplicationOrAttemptToAddItIfNotExisting(appId: String): Application? {
        // If the application already exists in the memory, return it.
        if (applicationMap.containsKey(appId)) {
            return applicationMap[appId]
        }

        // Check if the application is a legacy win32 app or a UWP app
        val isUWPApp = appId.startsWith("store:")

        try {
            val application = if (isUWPApp) {
                MSUWPApplication(appId)
            }else{
                MSLegacyApplication(this, appId)
            }

            // Save it in the external application storage
            externalAppManager.externalAppIds.add(appId)
            externalAppManager.persist()

            // Update the data structure
            applicationMap[appId] = application

            return application
        }catch (ex: ApplicationCreateException) {
            LOG.warning("$ex")
        }

        return null
    }

    /**
     * @return the active Application
     */
    override fun getActiveApplication(): Application? {
        var activeApplication : Application? = null
        WinApplicationLib.INSTANCE.getActiveApplication { _, _, _, appId ->
            activeApplication = getApplicationOrAttemptToAddItIfNotExisting(appId.toString())
        }
        return activeApplication
    }

    override fun getActiveApplications(): List<Application> {
        val apps = mutableListOf<Application>()

        WinApplicationLib.INSTANCE.listActiveApplications { _, _, _, appId ->
            val app = getApplicationOrAttemptToAddItIfNotExisting(appId.toString())
            app?.let { apps.add(app) }
        }

        return apps
    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses a kernel call to obtain it.
     *
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private fun getExecutablePathFromPID(pid: Int): String? {
        val pathText = ByteArray(1024)
        val process = Kernel32.INSTANCE.OpenProcess(0x0400 or 0x0010, false, pid)
        PsApi.INSTANCE.GetModuleFileNameExA(process, null, pathText, 1024)
        val executablePath = Native.toString(pathText)

        // If the executablePath is empty, return null
        return if (executablePath.length == 0) {
            null
        } else executablePath

    }

    data class AppTarget(val executablePath: String, val targetName: String?)

    /**
     * Load the Application(s) installed in the system.
     * Each time it is called, the list is refreshed.
     * A listener can be specified to monitor the status of the process.
     * This function checks in the Windows Start Menu and analyzes the entries.
     */
    override fun loadApplications(listener: ApplicationManager.OnLoadApplicationsListener?) {
        // Initialize the application maps
        applicationMap = CaseInsensitiveMap()

        // Get the links from the system start menu
        val linkFileList = loadApplicationLinksFromStartMenu()

        // Extract the targets
        val linkTargets = extractTargetsFromLinks(linkFileList) { appName, current, total ->
            listener?.onPreloadUpdate(appName, current, total)
        }

        // Populate the list

        // Load external targets
        val externalTargets = externalAppManager.externalAppIds.map {
            AppTarget(it, null)
        }

        val targets = mutableListOf<AppTarget>()
        targets.addAll(linkTargets)
        targets.addAll(externalTargets)

        val total = targets.size

        targets.forEachIndexed { current, target ->
            // Make sure the executable file still exists
            if (File(target.executablePath).isFile) {
                // Add the application
                val app = MSLegacyApplication(this, target.executablePath, target.targetName)

                applicationMap[target.executablePath] = app

                // Update the listener
                listener?.onProgressUpdate(app.name, current, total)
            }
        }

        // Signal the end of the process
        listener?.onApplicationsLoaded()

        // Mark the apps as initialized
        setInitialized()
    }

    private fun loadApplicationLinksFromStartMenu(): List<File> {
        // Get the start menu directories from the registry
        val userStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Start Menu")
        val systemStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Common Start Menu")
        val pathsToScan = arrayOf(userStartDir, systemStartDir)
        val extensionsToMatch = arrayOf("lnk")
        val linkFileList = ArrayList<File>()

        // Find all links recursively
        for (startPath in pathsToScan) {
            val files = FileUtils.listFiles(File(startPath), extensionsToMatch, true)
            linkFileList.addAll(files)
        }

        return linkFileList
    }

    private fun extractTargetsFromLinks(linkFileList: List<File>, onUpdate: ((appName: String, current: Int, total: Int) -> Unit)?): List<AppTarget> {
        val output = mutableListOf<AppTarget>()
        var current = 0
        var total = linkFileList.size

        for (file in linkFileList) {
            try {
                // Skip uninstallers
                if (file.name.toLowerCase().contains("uninstall")) {
                    LOG.fine("SKIP :" + file.absolutePath)
                    continue
                }

                val applicationName = file.name.replace(".lnk", "")
                val executablePath: String?

                // Try to load the executable path from the cache
                if (linkCacheManager.linkCache.containsKey(file.absolutePath)) {  // APP in cache
                    executablePath = linkCacheManager.linkCache[file.absolutePath]

                    // Check if the requested executable path should be skipped
                    if (executablePath == "SKIP") {
                        continue
                    }
                } else {  // APP not in cache
                    // Calculate the correct values
                    executablePath = ShellLinkResolver.resolveLnkTarget(file.absolutePath)

                    // Make sure the executable exists
                    if (executablePath != null && executablePath.endsWith(".exe")) {
                        // Save the destination to the cache
                        linkCacheManager.linkCache[file.absolutePath] = executablePath
                    } else {
                        // Mark the link as skippable
                        linkCacheManager.linkCache[file.absolutePath] = "SKIP"
                    }
                }

                // Filter out applications inside the C:\Windows folder
                if (executablePath?.startsWith("C:\\Windows", ignoreCase = true) == true) {
                    continue
                }

                if (executablePath != null) {
                    val target = AppTarget(executablePath, applicationName)
                    output.add(target)

                    // Update the listener
                    onUpdate?.invoke(applicationName, current, total)
                }
            } catch (e: Exception) {
                LOG.info("EXCEPTION WITH APP " + file.name + " " + e.toString())
            }

            current++
        }

        // Save the link cache to file
        linkCacheManager.persist()

        return output
    }

    /**
     * Return a list of Application(s) installed in the system.
     * Must be called after "loadApplications()".
     *
     * @return the list of Application installed in the system.
     */
    override fun getApplicationList(): List<Application> {
        return ArrayList(this.applicationMap.values)
    }

    /**
     * Calculate the application name by extracting it from the executable path
     * @param executablePath path to the app exe
     * @return the extracted name.
     */
    private fun calculateAppNameFromExecutablePath(executablePath: String): String? {
        val appExe = File(executablePath)
        // Create the new app name extracting the filename, removing the extension
        // and capitalizing the first letter
        return StringUtils.capitalize(appExe.name.toLowerCase().replace(".exe", ""))
    }

    /**
     * Generate the icon file for the given executable
     *
     * @param executablePath the executable with the icon
     * @return the icon File
     */
    private fun generateIconFile(executablePath: String): File {
        // Obtain the application ID
        val appID = Application.getHashIDForExecutablePath(executablePath)

        // Get the icon file
        return File(storageManager.iconCacheDir, "$appID.png")
    }

    /**
     * Obtain the icon associated with the given executable.
     *
     * @param executablePath path to the executable.
     * @return the icon associated with the given executable.
     */
    fun getIconPath(executablePath: String): String {
        // Get the icon file
        var iconFile: File? = null

        // Generate the icon file
        iconFile = generateIconFile(executablePath)

        // If the file doesn't exist, it must be generated
        if (!iconFile.isFile) {
            iconFile = extractIcon(executablePath)
            LOG.fine("ICON EXTRACTED: $executablePath")
        }

        // Return the icon file path
        return iconFile!!.absolutePath
    }

    /**
     * Extract the icon from the given executable.
     *
     * @param executablePath the executable with the icon.
     * @return the icon image file. Return null if an error occurred.
     */
    private fun extractIcon(executablePath: String): File? {
        // Get the icon file
        val iconFile = generateIconFile(executablePath)

        // Try to generate the icon using the native method
        try {
            //File extractedIcon = extractIconUsingExe(executablePath, iconFile, true);
            val extractedIcon = extractIconUsingNativeLib(executablePath, iconFile, true)
            if (extractedIcon != null) {
                return extractedIcon
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Exe method had an exception, use the fallback methods.

        // The icon can be obtained in two ways, but using powershell the
        // resulting image is better ( higher resolution ).
        if (isPowerShellEnabled) {  // Best method
            // Extract the icon
            val ris = extractIconUsingPowershellScript(executablePath, iconFile.absolutePath)

            // An error occurred, return null
            return if (!ris) {
                null
            } else {
                // Return the icon file ( generate again the file to avoid problems )
                generateIconFile(executablePath)
            }
        } else {  // Not so good, but should do the trick
            var icon: Icon? = null
            icon = FileSystemView.getFileSystemView().getSystemIcon(File(executablePath))
            val iconImage = iconToImage(icon) as BufferedImage
            try {
                ImageIO.write(iconImage, "png", iconFile)
            } catch (e: IOException) {  // ERROR
                return null
            }

        }

        return iconFile
    }

    /**
     * Extract the icon from the executable using the powershell method
     *
     * @param executablePath  path of the executable
     * @param destinationFile path of the destination image file
     * @return true if succeeded, false otherwise.
     */
    private fun extractIconUsingPowershellScript(executablePath: String, destinationFile: String): Boolean {
        val runtime = Runtime.getRuntime()

        try {
            // Execute powershell
            val proc = runtime.exec(arrayOf("powershell", "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing')  | Out-Null ; [System.Drawing.Icon]::ExtractAssociatedIcon('$executablePath').ToBitmap().Save('$destinationFile'); echo 'ok'"))

            // If there was an error, return false
            if (proc.errorStream.available() > 0) {
                return false
            }

            // Make sure everything was ok
            val br = BufferedReader(InputStreamReader(proc.inputStream))
            val line = br.readLine()
            if (line != null && line == "ok") {
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * Extract the icon from the executable using the native lib method
     *
     * @param executablePath  path of the executable
     * @param destinationFile path of the destination image file
     * @param bigIcon if true, request the 256x256 icon. If false 48x48 is requested.
     * @return true if succeeded, false otherwise.
     */
    fun extractIconUsingNativeLib(executablePath: String, destinationFile: File, bigIcon: Boolean): File? {
        var destinationFile = destinationFile
        try {
            WinExtractIconLib.extractIcon(executablePath, destinationFile.absolutePath, bigIcon)

            // If a big icon has been requested, make sure the resulting icon is valid.
            if (bigIcon && destinationFile.isFile) {
                // Reload the destination file
                destinationFile = File(destinationFile.absolutePath)

                // If the image is low resolution, request the 48x48 image.
                val image = ImageIO.read(destinationFile)
                if (isLowResImage(image)) {
                    return extractIconUsingNativeLib(executablePath, destinationFile, false)
                }
            }

            return destinationFile
        } catch (e: IOException) {
            println(executablePath)
            e.printStackTrace()
        }

        return null
    }

    companion object {
        // Create the logger
        private val LOG = Logger.getGlobal()

        /**
         * Check if powershell is enabled in this machine.
         *
         * @return true if powershell is enabled, else otherwise.
         */
        private fun checkPowerShellEnabled(): Boolean {
            val runtime = Runtime.getRuntime()

            try {
                // Execute powershell
                val proc = runtime.exec(arrayOf("powershell", "echo yes"))

                // If there was an error, return false
                if (proc.errorStream.available() > 0) {
                    return false
                }

                // Make sure the line is correct
                val br = BufferedReader(InputStreamReader(proc.inputStream))
                val line = br.readLine()
                if (line != null && line == "yes") {
                    return true
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }

        /**
         * Check if the given image is low res ( smaller than 48x48 ).
         * Used to filter big images 256x256 with small icons on the top left corner.
         * @param image the image to analyze.
         * @return true if image is smaller than 48x48, false otherwise.
         */
        fun isLowResImage(image: BufferedImage): Boolean {
            if (image.height < 250)
                return true

            var index = image.height - 1
            while (image.getRGB(index, index) == 0 && index > 0) {
                index--
            }

            return index < 48
        }

        /**
         * Converts an icon to a buffered image
         *
         * @param icon the icon to convert
         * @return the BufferedImage with the icon
         */
        private fun iconToImage(icon: Icon?): Image {
            if (icon is ImageIcon) {
                return icon.image
            } else {
                val w = icon!!.iconWidth
                val h = icon.iconHeight
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                val gd = ge.defaultScreenDevice
                val gc = gd.defaultConfiguration
                val image = gc.createCompatibleImage(w, h)
                val g = image.createGraphics()
                icon.paintIcon(null, g, 0, 0)
                g.dispose()
                return image
            }
        }
    }
}
