package system.model;

import system.CacheManager;
import system.model.Window;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationManager {
    // Name of the cache directory created in the home of the user
    public static final String CACHE_DIRECTORY_NAME = ".dokey";
    public static final String ICON_CACHE_DIRECTORY_NAME = "icons";
    public static final String EXTERNAL_APP_LIST_FILENAME = "externalapps.txt";

    /**
     * @return the Window object of the active system.window.
     */
    public abstract Window getActiveWindow();

    /**
     * @return the PID of the active system.window.
     */
    public abstract int getActivePID();

    /**
     * @return the active Application
     */
    public abstract Application getActiveApplication();

    /**
     * @return the List of active Application(s)
     */
    public abstract List<Application> getActiveApplications();

    /**
     * Get a list of Window currently active.
     */
    public abstract List<Window> getWindowList();

    /**
     * Load the Application(s) installed in the system.
     */
    public abstract void loadApplications(OnLoadApplicationsListener listener);

    /**
     * @return the list of Application(s) installed in the system.
     */
    public abstract List<Application> getApplicationList();

    /**
     * Get the application associated with the given executable path.
     * @param executablePath the path to the application.
     * @return the Application associated with the executable path if found, null otherwise.
     */
    public abstract Application getApplication(String executablePath);

    /**
     * Focus an application if already open or start it if not.
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean openApplication(String executablePath);

    /**
     * Return the icon file associated with the specified application.
     * @param executablePath path to the application
     * @return the icon image File object.
     */
    public abstract File getApplicationIcon(String executablePath);

    /**
     * Open the specified folder in the file explorer of the current OS.
     * @param folderPath the path to the folder
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean openFolder(String folderPath);

    /**
     * Open the specified url in the default browser.
     * @param url the web url of the page.
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean openWebLink(String url);

    /**
     * Interface used to update the status of the "loadApplications" operation.
     */
    public interface OnLoadApplicationsListener {
        void onPreloadUpdate(String applicationName, int current, int total);
        void onProgressUpdate(String applicationName, String iconPath, int current, int total);
        void onApplicationsLoaded();
    }

    /**
     * Load and return the application list of the user configured external applications
     * @return a List of executable path strings.
     */
    public List<String> loadExternalAppPaths() {
        // Get the cache manager
        CacheManager cacheManager = CacheManager.getInstance();

        List<String> output = new ArrayList<>();

        // Get the external app list file
        File externalAppsFile = new File(cacheManager.getCacheDir(), EXTERNAL_APP_LIST_FILENAME);

        // If the file exists, read it
        if (externalAppsFile.isFile()) {
            // Open the file and cycle through all the applications
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(externalAppsFile)))) {
                String applicationPath;
                while ((applicationPath = br.readLine()) != null) {
                    // Append the application
                    if (applicationPath != null && !applicationPath.isEmpty()) {
                        output.add(applicationPath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }

    /**
     * @return true if the given executable path is already present in the list, false otherwhise.
     */
    public abstract boolean isApplicationAlreadyPresent(String executablePath);

    /**
     * Add the given executable path to the external application list
     * @param executablePath the path of the application to add.
     * @return true if succeeded, false otherwise
     */
    public boolean addExternalApplication(String executablePath) {
        if (!isApplicationAlreadyPresent(executablePath)) {
            // Get the cache manager
            CacheManager cacheManager = CacheManager.getInstance();

            // Get the external app list file
            File externalAppsFile = new File(cacheManager.getCacheDir(), EXTERNAL_APP_LIST_FILENAME);

            // Open the file
            try (FileWriter fw = new FileWriter(externalAppsFile, true)) {
                // Append the info
                fw.write(executablePath);
                fw.write('\n');

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // Reload the application list
            loadApplications(null);

            return true;
        }

        return false;
    }

    /**
     * Remove the given executable path from the external application list
     * @param executablePath the path of the application to remove.
     * @return true if succeeded, false otherwise
     */
    public boolean removeExternalApplication(String executablePath) {
        if (isApplicationAlreadyPresent(executablePath)) {
            List<String> externalApps = loadExternalAppPaths();

            // Remove the element from the list
            externalApps.remove(executablePath);

            // Write the list again

            // Get the cache manager
            CacheManager cacheManager = CacheManager.getInstance();

            // Get the external app list file
            File externalAppsFile = new File(cacheManager.getCacheDir(), EXTERNAL_APP_LIST_FILENAME);

            // Open the file
            try (FileWriter fw = new FileWriter(externalAppsFile, false)) {
                for (String currentPath : externalApps) {
                    // Append the info
                    fw.write(currentPath);
                    fw.write('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // Reload the application list
            loadApplications(null);

            return true;
        }

        return false;
    }
}
