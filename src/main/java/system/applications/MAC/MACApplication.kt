package system.applications.MAC

import system.applications.Application
import java.io.File

/**
 * Represents an application installed in the system
 */
class MACApplication(name: String, val executablePath: String, iconPath: String?) :
        Application(executablePath, name, iconPath) {

    private val _globalId : String
    override val globalId: String
        get() = _globalId

    init {
        val executableFile = File(executablePath)
        _globalId = executableFile.name
    }

    /**
     * Open an application.
     */
    override fun open() : Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf("open", executablePath))
            return true
        }catch (e : Exception) {
            e.printStackTrace()
            return false
        }
    }
}