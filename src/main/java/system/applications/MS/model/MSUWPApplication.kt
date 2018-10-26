package system.applications.MS.model

import system.applications.Application

/**
 * Represents a UWP windows application installed in the system.
 * These category also includes the WindowsStore apps.
 */
class MSUWPApplication(id: String) : Application(id) {

    override val name: String
        get() = id  // TODO: change
    override val iconPath: String?
        get() = null // TODO: change

    init {
        // TODO: check existence using extractUWPApplicationDirectory
    }

    /**
     * Open an application.
     */
    override fun open(): Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf("explorer.exe", "shell:AppsFolder\\$id"))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}