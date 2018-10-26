package system.applications

import org.apache.commons.codec.digest.DigestUtils
import system.ResourceUtils
import java.io.File

/**
 * Represents an application installed in the system
 */
abstract class Application(val id: String) : Comparable<Application>{
    abstract val name: String
    abstract val iconPath: String?

    /**
     * This id can be used to identify the app globally, independently of the computer.
     * For example, for most apps these will be the application file name, without the local path.
     */
    open val globalId = id

    /**
     * Open an application.
     * @return true if opened correctly
     */
    abstract fun open() : Boolean

    override fun compareTo(other: Application): Int {
        return this.name.toLowerCase().compareTo(other.name.toLowerCase())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Application

        if (id != other.id) return false
        if (name != other.name) return false
        if (iconPath != other.iconPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (iconPath?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Application(id='$id', name='$name', iconPath=$iconPath, globalId='$globalId')"
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
}