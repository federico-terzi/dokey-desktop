package system.applications.MAC

import system.applications.Application

/**
 * Represents an application installed in the system
 */
class MACApplication(name: String, executablePath: String, iconPath: String?) :
        Application(name, executablePath, iconPath) {

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