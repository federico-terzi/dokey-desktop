package system.model

import java.io.File

/**
 * Represents an application installed in the system
 */
abstract class Application {
    val name : String
    val executablePath : String
    val iconPath : String?

    constructor(name: String, executablePath: String, iconPath: String?) {
        this.name = name
        this.executablePath = executablePath
        this.iconPath = iconPath
    }

    /**
     * Return the Icon image file, must be implemented.
     */
    abstract fun getIconFile() : File?
}