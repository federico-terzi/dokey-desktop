package system.storage;

import java.io.File;

public interface StorageManager {
    // Name of the cache directory created in the home of the user
    String DEFAULT_STORAGE_DIRECTORY_NAME = ".dokey";

    File getStorageDir();
    File getIconCacheDir();
    File getSectionDir();
    File getWebCacheDir();
    File getCommandDir();
    File getCacheDir();

    /**
     * Clear the app cache.
     * SECTIONS AND COMMANDS WILL NOT BE DELETED
     * @return true if succeeded, false otherwise.
     */
    boolean clearCache();


    /**
     * @return the user home directory
     */
    static File getUserHomeDir() {
        // Get the user home directory
        File homeDir = new File(System.getProperty("user.home"));

        return homeDir;
    }

    /**
     * Create and retrieve the storage directory.
     *
     * @return the Storage directory used to save files.
     */
    static File getDefaultStorageDir() {
        // Get the user home directory
        File homeDir = getUserHomeDir();

        // Get the cache directory
        File storageDir = new File(homeDir, DEFAULT_STORAGE_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!storageDir.isDirectory()) {
            storageDir.mkdir();
        }

        return storageDir;
    }

    /**
     * @return the default StorageManager
     */
    static StorageManager getDefault() {
        return new BaseDirectoryStorageManager(getDefaultStorageDir());
    }
}
