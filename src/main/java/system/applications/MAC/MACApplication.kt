package system.applications.MAC

import system.applications.Application
import java.io.File

/**
 * Represents an application installed in the system
 */
class MACApplication(val applicationManager: MACApplicationManager, val appBundlePath: String) : Application(appBundlePath) {

    override val name: String
    override val iconPath: String by lazy {
        applicationManager.getIconPath(appBundlePath)
    }

    private val _globalId : String
    override val globalId: String
        get() = _globalId

    init {
        val appBundleDir = File(appBundlePath)
        _globalId = appBundleDir.name

        // Get the application name by removing the ".app" suffix
        name = appBundleDir.name.substring(0, appBundleDir.name.length - 4)
    }

    /**
     * Open an application.
     */
    override fun open() : Boolean {
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
}