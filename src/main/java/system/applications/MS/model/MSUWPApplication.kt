package system.applications.MS.model

import system.applications.Application

/**
 * Represents a UWP windows application installed in the system.
 * These category also includes the WindowsStore apps.
 */
class MSUWPApplication(id: String) : Application(id) {

    override val name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val iconPath: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

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