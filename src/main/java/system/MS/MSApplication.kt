package system.MS

import system.model.Application
import java.io.File

/**
 * Represents an application installed in the system
 */
class MSApplication(name: String, executablePath: String, iconPath: String?) :
        Application(name, executablePath, iconPath) {

    /**
     * Open an application.
     */
    override fun open() : Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(executablePath)
            return true
        }catch (e : Exception) {
            e.printStackTrace()
            return false
        }
    }
}