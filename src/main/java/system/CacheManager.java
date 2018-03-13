package system;


import org.apache.commons.io.FileUtils;
import system.model.ApplicationManager;
import system.section.SectionManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;

import static system.MS.MSApplicationManager.APP_CACHE_FILENAME;
import static system.MS.MSApplicationManager.START_MENU_CACHE_FILENAME;
import static system.model.ApplicationManager.INITIALIZED_CHECK_FILENAME;

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
    private File webCacheDir;
    private File sectionDir;

    private CacheManager() {
        cacheDir = loadCacheDir();
        iconCacheDir = loadIconCacheDir();
        webCacheDir = loadWebCacheDir();
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
     * Create and retrieve the web cache directory.
     *
     * @return the Web Cache directory used to save web icons.
     */
    private File loadWebCacheDir() {
        File cacheDir = loadCacheDir();

        // Get the icon cache directory
        File webCacheDir = new File(cacheDir, WebLinkResolver.WEB_CACHE_DIRNAME);

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
        File cacheDir = loadCacheDir();

        // Get the icon cache directory
        File sectionDir = new File(cacheDir, SectionManager.SECTION_FOLDER_NAME);

        // If it doesn't exists, createRequest it
        if (!sectionDir.isDirectory()) {
            sectionDir.mkdir();
        }

        return sectionDir;
    }

    /**
     * Clear the app cache, by deleting the icons and the cached apps.
     * SECTIONS WILL NOT BE DELETED
     * @return true if succeeded, false otherwise.
     */
    public boolean clearCache() {
        boolean result = true;

        // Delete the icon folder
        File iconDir = getIconCacheDir();
        if (iconDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(iconDir);
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }

        // Delete the web cache
        File webDir = getWebCacheDir();
        if (webDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(webDir);
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }

        // Delete the app cache files
        if (OSValidator.isWindows()) {
            File appCache = new File(getCacheDir(), APP_CACHE_FILENAME);
            if (appCache.isFile()) {
                result = result && appCache.delete();
            }

            File startMenuCache = new File(getCacheDir(), START_MENU_CACHE_FILENAME);
            if (startMenuCache.isFile()) {
                result = result && startMenuCache.delete();
            }

        }

        // Delete the initialized check
        File initializedFile = new File(cacheDir, INITIALIZED_CHECK_FILENAME);
        if (initializedFile.isFile()) {
            result = result & initializedFile.delete();
        }

        return result;
    }


    /**
     * @return the user home directory
     */
    public File getUserHomeDir() {
        // Get the user home directory
        File homeDir = new File(System.getProperty("user.home"));

        return homeDir;
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

    public File getWebCacheDir() {
        return webCacheDir;
    }
}
