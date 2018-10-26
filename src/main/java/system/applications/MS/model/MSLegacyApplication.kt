package system.applications.MS.model

import org.apache.commons.lang3.StringUtils
import system.applications.Application
import system.applications.MS.MSApplicationManager
import system.applications.MS.model.exception.ApplicationNotFoundException
import java.io.File

/**
 * Represents a classic Win32 application installed in the system.
 */
class MSLegacyApplication(val applicationManager: MSApplicationManager, val executablePath: String,
                          val _name: String? = null) :
        Application(executablePath) {

    override val name: String

    // Icon path property is resolved lazily
    override val iconPath: String by lazy {  // TODO: may need to change synchronization options.
        // Extract the icon dynamically
        applicationManager.getIconPath(executablePath)
    }

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

        _globalId = executableFile.name
    }

    /**
     * Open an application.
     */
    override fun open() : Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf(executablePath))
            return true
        }catch (e : Exception) {
            e.printStackTrace()
            return false
        }
    }

    companion object {
        /**
         * Calculate the application name by extracting it from the executable path
         * @param executablePath path to the app exe
         * @return the extracted name.
         */
        private fun calculateAppNameFromExecutablePath(executablePath: String): String {
            val appExe = File(executablePath)
            // Create the new app name extracting the filename, removing the extension
            // and capitalizing the first letter
            return StringUtils.capitalize(appExe.name.toLowerCase().replace(".exe", ""))
        }
    }
}