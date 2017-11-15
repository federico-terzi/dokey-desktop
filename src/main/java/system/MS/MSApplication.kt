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
    override fun open() {
        val runtime = Runtime.getRuntime()

        // Execute the process
        val proc = runtime.exec(executablePath)
    }
}