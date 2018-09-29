package system

import system.startup.StartupManager
import system.storage.StorageManager

import java.io.*
import java.util.Properties
import java.util.logging.Logger

const val SETTINGS_FILENAME = "settings.properties"

// Settings properties
const val ENABLE_DOKEY_SEARCH = "ENABLE_DOKEY_SEARCH"
const val SHOW_DELETED_COMMANDS = "SHOW_DELETED_COMMANDS"

// Default settings
const val DEFAULT_ENABLE_DOKEY_SEARCH = true.toString()
const val DEFAULT_SHOW_DELETED_COMMANDS = false.toString()

/**
 * Used to save, retrieve and manage user settings.
 */
class SettingsManager(storageManager: StorageManager) {
    // Create the logger
    private val LOG = Logger.getGlobal()

    private val properties = Properties()
    private val settingsFile = File(storageManager.storageDir, SETTINGS_FILENAME)

    // Specific properties

    /**
     * true if dokey search is enabled, false otherwise.
     */
    var dokeySearchEnabled : Boolean
        get() = properties.getProperty(ENABLE_DOKEY_SEARCH, DEFAULT_ENABLE_DOKEY_SEARCH).toBoolean()
        set(isEnabled) {
            properties.setProperty(ENABLE_DOKEY_SEARCH, isEnabled.toString())
            // Send the broadcast
            BroadcastManager.getInstance().sendBroadcast(BroadcastManager.ENABLE_DOKEY_SEARCH_PROPERTY_CHANGED, isEnabled)
            savePreferences()
        }

    var showDeletedCommands : Boolean
        get() = properties.getProperty(SHOW_DELETED_COMMANDS, DEFAULT_SHOW_DELETED_COMMANDS).toBoolean()
        set(isEnabled) {
            properties.setProperty(SHOW_DELETED_COMMANDS, isEnabled.toString())
            savePreferences()
        }

    init {

        if (!settingsFile.isFile) {  // NO settings file available.
            // Default settings
            properties.setProperty(ENABLE_DOKEY_SEARCH, DEFAULT_ENABLE_DOKEY_SEARCH)
            properties.setProperty(SHOW_DELETED_COMMANDS, DEFAULT_SHOW_DELETED_COMMANDS)

            // Save the default settings
            savePreferences()
        } else {  // File exist, load settings from it
            // Load the properties
            try {
                val fis = FileInputStream(settingsFile)
                properties.load(fis)
                fis.close()
            } catch (e: IOException) {
                LOG.severe("Cannot load project settings")
            }

        }
    }

    /**
     * Save properties to file.
     * @return true if succeeded, false otherwise.
     */
    private fun savePreferences(): Boolean {
        try {
            val fos = FileOutputStream(settingsFile)
            properties.store(fos, "Dokey Settings")
            fos.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }
}
