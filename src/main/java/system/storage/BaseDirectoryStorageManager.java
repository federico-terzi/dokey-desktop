package system.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static system.applications.ApplicationManager.INITIALIZED_CHECK_FILENAME;

/**
 * Used to manage files and folders
 */
public class BaseDirectoryStorageManager implements StorageManager {
    public static final String CACHE_DIRECTORY_NAME = "cache";

    // Specific directory names
    // PERSISTENT
    public static final String SECTION_DIRECTORY_NAME = "sections";
    public static final String COMMAND_DIRECTORY_NAME = "commands";

    // CACHED
    public static final String ICON_CACHE_DIRECTORY_NAME = "icons";
    public static final String WEB_CACHE_DIRECTORY_NAME = "webcache";

    private File storageDir;
    private File cacheDir;
    private File iconCacheDir;
    private File webCacheDir;
    private File sectionDir;
    private File commandDir;

    public BaseDirectoryStorageManager(File storageDir) {
        this.storageDir = storageDir;

        cacheDir = loadCacheDir();
        iconCacheDir = loadIconCacheDir();
        webCacheDir = loadWebCacheDir();
        sectionDir = loadSectionDir();
        commandDir = loadCommandDir();
    }

    /**
     * Create and retrieve the cache directory.
     *
     * @return the Cache directory used to save images.
     */
    public File loadCacheDir() {
        File storageDir = getStorageDir();

        // Get the cache directory
        File cacheDir = new File(storageDir, CACHE_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!cacheDir.isDirectory()) {
            cacheDir.mkdir();
        }

        return cacheDir;
    }

    /**
     * Create and retrieve the image cache directory.
     *
     * @return the Image Cache directory used to save images.
     */
    public File loadIconCacheDir() {
        File cacheDir = getCacheDir();

        // Get the cache directory
        File iconCacheDir = new File(cacheDir, ICON_CACHE_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!iconCacheDir.isDirectory()) {
            iconCacheDir.mkdir();
        }

        return iconCacheDir;
    }

    /**
     * Create and retrieve the web cache directory.
     *
     * @return the Web Cache directory used to save web icons.
     */
    private File loadWebCacheDir() {
        File cacheDir = loadCacheDir();

        // Get the cache directory
        File webCacheDir = new File(cacheDir, WEB_CACHE_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!webCacheDir.isDirectory()) {
            webCacheDir.mkdir();
        }

        return webCacheDir;
    }

    /**
     * Create and retrieve the section directory.
     *
     * @return the Section Cache directory.
     */
    public File loadSectionDir() {
        File storageDir = getStorageDir();

        // Get the cache directory
        File sectionDir = new File(storageDir, SECTION_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!sectionDir.isDirectory()) {
            sectionDir.mkdir();
        }

        return sectionDir;
    }

    /**
     * Create and retrieve the commands directory.
     *
     * @return the quick commands directory.
     */
    public File loadCommandDir() {
        File storageDir = getStorageDir();

        // Get the cache directory
        File commandDir = new File(storageDir, COMMAND_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!commandDir.isDirectory()) {
            commandDir.mkdir();
        }

        return commandDir;
    }

    /**
     * Clear the app cache.
     * SECTIONS AND COMMANDS WILL NOT BE DELETED
     * @return true if succeeded, false otherwise.
     */
    public boolean clearCache() {
        boolean result = true;

        // Delete the cache folder
        try {
            FileUtils.deleteDirectory(cacheDir);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        // Delete the initialized check
        File initializedFile = new File(storageDir, INITIALIZED_CHECK_FILENAME);
        if (initializedFile.isFile()) {
            result = result & initializedFile.delete();
        }

        return result;
    }

    public File getStorageDir() {
        return storageDir;
    }

    public File getIconCacheDir() {
        return iconCacheDir;
    }

    public File getSectionDir() {
        return sectionDir;
    }

    public File getWebCacheDir() {
        return webCacheDir;
    }

    public File getCommandDir() {
        return commandDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }
}
