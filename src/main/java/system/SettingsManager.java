package system;

import system.storage.StorageManager;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Used to save, retrieve and manage user settings.
 */
public class SettingsManager {
    public static final String SETTINGS_FILENAME = "settings.properties";

    // Settings properties
    public static final String ENABLE_DOKEY_SEARCH = "ENABLE_DOKEY_SEARCH";

    // Default settings
    public static final String DEFAULT_ENABLE_DOKEY_SEARCH = String.valueOf(true);

    private Properties properties = new Properties();
    private StorageManager storageManager;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public SettingsManager(StorageManager storageManager) {
        this.storageManager = storageManager;

        if (!getSettingsFile().isFile()) {  // NO settings file available.
            // Default settings
            properties.setProperty(ENABLE_DOKEY_SEARCH, DEFAULT_ENABLE_DOKEY_SEARCH);

            // Save the default settings
            savePreferences();
        }else{  // File exist, load settings from it
            // Load the properties
            try {
                FileInputStream fis = new FileInputStream(getSettingsFile());
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                LOG.severe("Cannot load project settings");
            }
        }
    }

    /**
     * Save properties to file.
     * @return true if succeeded, false otherwise.
     */
    private boolean savePreferences() {
        try {
            FileOutputStream fos = new FileOutputStream(getSettingsFile());
            properties.store(fos, "Dokey Settings");
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return the file where settings are saved to.
     */
    private File getSettingsFile() {
        return new File(storageManager.getStorageDir(), SETTINGS_FILENAME);
    }

    // Specific properties

    /**
     * @return true if dokey search is enabled, false otherwise.
     */
    public boolean isDokeySearchEnabled() {
        return Boolean.valueOf(properties.getProperty(ENABLE_DOKEY_SEARCH, DEFAULT_ENABLE_DOKEY_SEARCH));
    }

    /**
     * Set the dokey search enabled property, save the settings and send the broadcast.
     * @param isEnabled
     * @return true if succeeded, false otherwise.
     */
    public boolean setDokeySearchEnabled(boolean isEnabled) {
        properties.setProperty(ENABLE_DOKEY_SEARCH, String.valueOf(isEnabled));
        // Send the broadcast
        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.ENABLE_DOKEY_SEARCH_PROPERTY_CHANGED, isEnabled);
        return savePreferences();
    }
}
