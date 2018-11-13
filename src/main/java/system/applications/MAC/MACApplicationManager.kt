package system.applications.MAC

import com.sun.jna.Native
import com.sun.jna.Pointer
import system.storage.StorageManager
import system.ResourceUtils
import system.startup.StartupManager
import system.applications.Application
import system.applications.ApplicationManager
import system.applications.ExternalAppManager

import java.io.*
import java.net.URL
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors

class MACApplicationManager(storageManager: StorageManager, private val startupManager: StartupManager) : ApplicationManager(storageManager) {

    // This map will hold the applications, associated with their executable path
    private var applicationMap: MutableMap<String, Application> = HashMap()

    private val externalAppManager = ExternalAppManager(storageManager)

    init {
        // Initialize the external app manager
        externalAppManager.load()
    }

    /**
     * Focus an application if already open or start it if not.
     *
     * @param applicationId path to the application.
     * @return true if succeeded, false otherwise.
     */
    @Synchronized
    override fun openApplication(applicationId: String?): Boolean {
        if (applicationId == null)
            return false

        // Try to focus the application directly if already running
        val result = MacApplicationLib.INSTANCE.activateRunningApplication(applicationId)
        if (result > 0) {
            return true
        }

        // The application was not running, we need to get a reference to it
        val application: Application? = getApplicationOrAttemptToAddItIfNotExisting(applicationId)

        // Open it
        if (application != null) {
            application.open()
        } else {
            return false
        }

        var waitAmount: Long = 0
        while (waitAmount < OPEN_APPLICATION_TIMEOUT) {
            // Check if the application has taken focus
            val activeApp = activeApplication
            if (activeApp != null && activeApp.id == applicationId) {
                return true
            }

            // Retry to focus the app
            MacApplicationLib.INSTANCE.activateRunningApplication(applicationId)

            // Sleep for a bit to give the application some time to open
            try {
                Thread.sleep(OPEN_APPLICATION_CHECK_INTERVAL)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            waitAmount += OPEN_APPLICATION_CHECK_INTERVAL
        }

        return false
    }

    /**
     * Return the icon file associated with the specified application.
     * @param applicationId path to the application
     * @return the icon image File object.
     */
    override fun getApplicationIcon(applicationId: String): File? {
        // Get the application
        val application: Application? = getApplicationOrAttemptToAddItIfNotExisting(applicationId)

        // Make sure the application exists
        return if (application?.iconPath != null) {
            File(application.iconPath!!)
        } else {
            null
        }
    }

    override fun open(filePath: String): Boolean {
        // Make sure the file/folder exists
        val file = File(filePath)
        if (!file.isDirectory && !file.isFile) {
            return false
        }

        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("open", filePath))
            proc.waitFor()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
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
            val proc = runtime.exec(arrayOf("open", url))
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

        val escapedCommand = command.replace("\"", "\\\"")

        try {
            // Execute the process
            runtime.exec(arrayOf("osascript", "-e", "tell application \"Terminal\" to do script \"$escapedCommand\""))

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    override fun focusDokey(): Boolean {
        return MacApplicationLib.INSTANCE.focusDokey() == 1
    }

    override fun focusSearch(): Boolean {
        return focusDokey()
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
     * Get the current active application by using objective c runtime bindings.
     * @return the active Application
     */
    override fun getActiveApplication(): Application? {
        val pathBuffer = ByteArray(512)
        MacApplicationLib.INSTANCE.getActiveApplication(pathBuffer, pathBuffer.size)

        val path = Native.toString(pathBuffer)

        if (File(path).isDirectory) {
            // Try to get it from the applicationMap
            return getApplicationOrAttemptToAddItIfNotExisting(path)
        }

        return null
    }

    override fun getActiveApplications(): List<Application> {
        val apps = mutableListOf<Application>()

        MacApplicationLib.INSTANCE.getActiveApplications { appPath ->
            val app: Application? = getApplicationOrAttemptToAddItIfNotExisting(appPath)

            if (app != null && !apps.contains(app)) {
                apps.add(app)
            }
        }

        return apps
    }

    /**
     * Get the current active pid by using objective c runtime bindings.
     */
    override fun getActivePID(): Int {
        return MacApplicationLib.INSTANCE.activePID
    }

    /**
     * Load the Application(s) installed in the system.
     * Each time it is called, the list is refreshed.
     * A listener can be specified to monitor the status of the process.
     * This function checks in the Applications folder.
     */
    override fun loadApplications(listener: ApplicationManager.OnLoadApplicationsListener?) {
        // Initialize the application maps
        applicationMap = HashMap()

        // Create a list that will hold all the files
        val fileList = ArrayList<File>()

        val runtime = Runtime.getRuntime()

        try {
            // Get the list of apps
            val proc = runtime.exec(arrayOf("find", "/Applications", "-name", "*.app", "-maxdepth", "3"))

            proc.inputStream.bufferedReader().useLines { it.forEach { line ->
                // Skip nested applications
                if (line.split(".app").size == 2) {
                    val currentAppDir = File(line)
                    fileList.add(currentAppDir)
                }
            }}
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        // Load the list of the external applications
        val externalTargets = externalAppManager.externalAppIds.map {
            File(it)
        }.filter { it.isDirectory }
        fileList.addAll(externalTargets)

        // Current application in the list
        var current = 0

        // Cycle through all entries
        for (app in fileList) {
            try {
                val appPath = app.absolutePath

                // Add the application
                val application = getApplicationOrAttemptToAddItIfNotExisting(appPath,
                                    addToExternalApplications= false)

                // Update the listener and increase the counter
                if (listener != null && application != null) {
                    listener.onProgressUpdate(application.name, current, fileList.size)
                }
            } catch (e: Exception) {
                LOG.info("EXCEPTION WITH APP " + app.name + " " + e.toString())
            }

            current++
        }

        // Signal the end of the process
        listener?.onApplicationsLoaded()

        // Mark the apps as initialized
        setInitialized()
    }

    /**
     * Parse and analyze the application from the app folder. Then add it to the applicationMap.
     *
     * @param appPath path to the app folder
     * @return the Application object.
     */
    @Synchronized
    private fun getApplicationOrAttemptToAddItIfNotExisting(appPath: String,
                                                            addToExternalApplications: Boolean = true): Application? {
        // Return directly if the application was already loaded
        if (applicationMap.containsKey(appPath)) {
            return applicationMap[appPath]
        }

        // Exclude the finder from the list
        if (appPath.endsWith("Finder.app")) {
            return null
        }

        // Make sure the target is an app
        if (appPath.toLowerCase().endsWith(".app")) {
            // Create the application
            val application = MACApplication(storageManager, appPath)

            if (addToExternalApplications) {
                // Save it in the external application storage
                externalAppManager.externalAppIds.add(appPath)
                externalAppManager.persist()
            }

            // Add it to the map
            applicationMap[appPath] = application

            return application
        }
        return null
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

    companion object {

        // Create the logger
        private val LOG = Logger.getGlobal()

        private val OPEN_APPLICATION_TIMEOUT: Long = 3000  // Timeout for the open application request.

        private val OPEN_APPLICATION_CHECK_INTERVAL: Long = 100  // How often to check if an application has focus
    }
}
