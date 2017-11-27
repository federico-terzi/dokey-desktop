package system;


import system.model.ApplicationManager;

import java.io.File;

/**
 * Used to manage cache files and folders
 */
public class CacheManager {
    private static CacheManager ourInstance = new CacheManager();

    public static CacheManager getInstance() {
        return ourInstance;
    }

    private File cacheDir;
    private File iconCacheDir;
    private File sectionDir;

    private CacheManager() {
        cacheDir = loadCacheDir();
        iconCacheDir = loadIconCacheDir();
        sectionDir = loadSectionDir();
    }

    /**
     * Create and retrieve the cache directory.
     *
     * @return the Cache directory used to save files.
     */
    public File loadCacheDir() {
        // Get the user home directory
        File homeDir = new File(System.getProperty("user.home"));

        // Get the cache directory
        File cacheDir = new File(homeDir, ApplicationManager.CACHE_DIRECTORY_NAME);

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
        File cacheDir = loadCacheDir();

        // Get the icon cache directory
        File iconCacheDir = new File(cacheDir, ApplicationManager.ICON_CACHE_DIRECTORY_NAME);

        // If it doesn't exists, createRequest it
        if (!iconCacheDir.isDirectory()) {
            iconCacheDir.mkdir();
        }

        return iconCacheDir;
    }

    /**
     * Create and retrieve the section directory.
     *
     * @return the Section Cache directory.
     */
    public File loadSectionDir() {
        File cacheDir = loadCacheDir();

        // Get the icon cache directory
        File sectionDir = new File(cacheDir, SectionManager.SECTION_FOLDER_NAME);

        // If it doesn't exists, createRequest it
        if (!sectionDir.isDirectory()) {
            sectionDir.mkdir();
        }

        return sectionDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public File getIconCacheDir() {
        return iconCacheDir;
    }

    public File getSectionDir() {
        return sectionDir;
    }
}
