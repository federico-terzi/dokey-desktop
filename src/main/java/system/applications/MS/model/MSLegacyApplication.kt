package system.applications.MS.model

import org.apache.commons.lang3.StringUtils
import system.applications.Application
import system.applications.MS.MSApplicationManager
import system.applications.MS.WinExtractIconLib
import system.applications.MS.model.exception.ApplicationNotFoundException
import system.storage.StorageManager
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.logging.Logger
import javax.imageio.ImageIO

/**
 * Represents a classic Win32 application installed in the system.
 */
class MSLegacyApplication(val storageManager: StorageManager, val executablePath: String, _name: String? = null) :
        MSAbstractApplication(executablePath) {

    override val name: String
    override val iconPath: String?

    private val _globalId : String
    override val globalId: String
        get() = _globalId

    init {
        val executableFile = File(executablePath)

        // Make sure the application is valid
        if (!executableFile.isFile) {
            throw ApplicationNotFoundException("Cannot create Application for path: \"$executablePath\"")
        }

        // Determine the name of the application
        name = if (_name != null) {
            _name
        }else{
            calculateAppNameFromExecutablePath(executablePath)
        }

        // Extract the application icon
        iconPath = getIconPath(executablePath)

        _globalId = executableFile.name
    }

    /**
     * Open an application.
     */
    override fun open() : Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf("explorer.exe", executablePath))
            return true
        }catch (e : Exception) {
            e.printStackTrace()
            return false
        }
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
    private fun getIconPath(executablePath: String): String? {
        // Get the icon file
        var iconFile: File? = null

        // Generate the icon file
        iconFile = generateIconFile(executablePath)

        // If the file doesn't exist, it must be generated
        if (!iconFile.isFile) {
            iconFile = extractIcon(executablePath)

            if (iconFile != null) {
                LOG.fine("ICON EXTRACTED: $executablePath")
            }else{
                return null
            }
        }

        // Return the icon file path
        return iconFile.absolutePath
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
            //File extractedIcon = extractIconUsingExe(appId, iconFile, true);
            val extractedIcon = extractIconUsingNativeLib(executablePath, iconFile, true)
            if (extractedIcon != null) {
                return extractedIcon
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
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
         * Calculate the application name by extracting it from the executable path
         * @param executablePath path to the app exe
         * @return the extracted name.
         */
        fun calculateAppNameFromExecutablePath(executablePath: String): String {
            val appExe = File(executablePath)
            // Create the new app name extracting the filename, removing the extension
            // and capitalizing the first letter
            return StringUtils.capitalize(appExe.name.toLowerCase().replace(".exe", ""))
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
    }
}