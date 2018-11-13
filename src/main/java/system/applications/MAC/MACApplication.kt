package system.applications.MAC

import system.applications.Application
import system.storage.StorageManager
import java.io.File

/**
 * Represents an application installed in the system
 */
class MACApplication(val storageManager: StorageManager, val appBundlePath: String) : Application(appBundlePath) {

    override val name: String
    override val iconPath: String?

    private val _globalId : String
    override val globalId: String
        get() = _globalId

    init {
        val appBundleDir = File(appBundlePath)
        _globalId = appBundleDir.name

        // Get the application name by removing the ".app" suffix
        name = appBundleDir.name.substring(0, appBundleDir.name.length - 4)

        // Extract the icon ( or get it from the cache if available
        iconPath = getIconPath(appBundlePath)
    }

    /**
     * Open an application.
     */
    override fun open() : Boolean {
        // Initially try to focus directly the application if running.
        val result = MacApplicationLib.INSTANCE.activateRunningApplication(appBundlePath)

        if (result > 0) {
            return true
        }

        // The app is not running, start it.
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf("open", appBundlePath))
            return true
        }catch (e : Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Generate the icon file for the given app
     *
     * @param appPath the app folder
     * @return the icon File
     */
    private fun generateIconFile(appPath: String): File {
        // Obtain the application ID
        val appID = Application.getHashIDForExecutablePath(appPath)

        // Get the icon file
        return File(storageManager.iconCacheDir, "$appID.png")
    }

    /**
     * Obtain the icon associated with the given application.
     *
     * @param appPath path to the app folder.
     * @return the icon associated with the given app.
     */
    internal fun getIconPath(appPath: String): String? {
        // Get the icon file
        var iconFile: File? = generateIconFile(appPath)

        // If the file doesn't exist, it must be generated
        if (!iconFile!!.isFile) {
            iconFile = extractIcon(appPath)

            // App doesn't have an image, return null
            if (iconFile == null) {
                return null
            }
        }

        // Return the icon file path
        return iconFile.absolutePath
    }

    /**
     * Extract the icon from the given app.
     *
     * @param appPath the app folder.
     * @return the icon image file. Return null if an error occurred.
     */
    private fun extractIcon(appPath: String): File? {
        // Get the icon file
        val iconFile = generateIconFile(appPath)

        // Extract the icon using the native method
        MacApplicationLib.INSTANCE.extractApplicationIcon(appPath, iconFile.absolutePath)

        return iconFile
    }
}