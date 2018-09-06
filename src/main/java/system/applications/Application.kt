package system.applications

import org.apache.commons.codec.digest.DigestUtils
import system.ResourceUtils
import java.io.File

/**
 * Represents an application installed in the system
 */
abstract class Application : Comparable<Application>{
    val name : String
    val executablePath : String
    val iconPath : String?

    constructor(name: String, executablePath: String, iconPath: String?) {
        this.name = name
        this.executablePath = executablePath
        this.iconPath = iconPath
    }

    companion object {
        /**
         * Return the String Hash ID for the given executablePath
         * @return the MD5 hash of the executablePath
         */
        fun getHashIDForExecutablePath(executablePath: String) : String {
            return DigestUtils.md5Hex(executablePath)
        }
    }

    /**
     * Open an application.
     * @return true if opened correctly
     */
    abstract fun open() : Boolean

    /**
     * Return the String Hash ID of the application.
     * @return the MD5 hash of the executablePath
     */
    fun getHashID() : String {
        return getHashIDForExecutablePath(executablePath)
    }

    /**
     * Return the icon file associated with the application if available,
     * the fallback image otherwise.
     */
    fun getIconFile() : File {
        if (iconPath == null || !File(iconPath).isFile) {
            return ResourceUtils.getResource("/assets/photo.png")  // Default Fallback
        }else{
            return File(iconPath)  // Application icon
        }
    }

    override fun toString(): String {
        return "Application(name='$name', executablePath='$executablePath', iconPath=$iconPath)"
    }

    override fun compareTo(other: Application): Int {
        return this.name.toLowerCase().compareTo(other.name.toLowerCase())
    }
}