package system.server

import system.storage.StorageManager
import java.io.File
import java.io.PrintWriter
import java.security.SecureRandom
import kotlin.text.Charsets.UTF_8

const val KEY_SIZE = 10

/**
 * Create and manage the local computer key for the secure communication.
 */
class KeyGenerator(val storageManager: StorageManager) {
    var key : ByteArray? = null

    /**
     * If the key is not already present, create one and then load it.
     */
    fun initialize() {
        // If the key is not already present, create one
        if (!getKeyFile().isFile) {
            generateKeyFile()
        }

        // Load the key
        key = loadKey()
    }

    /**
     * Invalidate the current key and create a new one
     */
    fun invalidateKey() {
        getKeyFile().delete()
        generateKeyFile()
        key = loadKey()
    }

    /**
     * Load the key from the file
     */
    private fun loadKey(): ByteArray? {
        if (!getKeyFile().isFile) {
            return null
        }

        // Read all the key numbers from the file
        // NOTE: they are formatted like this
        // 1,2,3,4,5,6
        val keyString = getKeyFile().readText(UTF_8)
        val numbers = keyString.split(",").map { it.toByte() }
        return numbers.toByteArray()
    }

    /**
     * Generate a secure random key
     */
    private fun generateKey() : String {
        val random = SecureRandom()
        val key = ByteArray(KEY_SIZE)
        random.nextBytes(key)
        // Make all digits positive
        return key.map{ Math.abs(it.toInt()).toByte() }.toByteArray().joinToString(",")
    }

    /**
     * Generate the key file
     */
    private fun generateKeyFile() {
        val keyString = generateKey()
        getKeyFile().printWriter().use { out ->
            out.print(keyString)
        }
    }

    /**
     * Return the Key file in the storage directory
     */
    private fun getKeyFile() : File {
        return File(storageManager.storageDir, "local.key")
    }
}