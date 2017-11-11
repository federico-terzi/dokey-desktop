package system.MS

import system.model.Application
import java.io.File

/**
 * Represents an application installed in the system
 */
class MSApplication(name: String, executablePath: String, iconPath: String?) :
        Application(name, executablePath, iconPath) {

    /**
     * Return the image File of the Icon associated with the Application.
     */
    override fun getIconFile(): File? {
        return null
    }
}