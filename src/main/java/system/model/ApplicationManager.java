package system.model;

import system.StorageManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationManager {
    public static final String EXTERNAL_APP_LIST_FILENAME = "externalapps.txt";
    public static final String INITIALIZED_CHECK_FILENAME = "initialized.txt";

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
     * Open the System terminal and execute the specified command.
     * @param command the command to execute
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean openTerminalWithCommand(String command);

    /**
     * Focus Dokey if open.
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean focusDokey();

    /**
     * Focus Dokey search bar
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean focusSearch();

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
        StorageManager storageManager = StorageManager.getInstance();

        List<String> output = new ArrayList<>();

        // Get the external app list file
        File externalAppsFile = new File(storageManager.getStorageDir(), EXTERNAL_APP_LIST_FILENAME);

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
            StorageManager storageManager = StorageManager.getInstance();

            // Get the external app list file
            File externalAppsFile = new File(storageManager.getStorageDir(), EXTERNAL_APP_LIST_FILENAME);

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
            StorageManager storageManager = StorageManager.getInstance();

            // Get the external app list file
            File externalAppsFile = new File(storageManager.getStorageDir(), EXTERNAL_APP_LIST_FILENAME);

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

    /**
     * Check if the applications have already been initialized.
     * @return true if initialized, false otherwise.
     */
    public boolean isInitialized() {
        File cacheDir = StorageManager.getInstance().getStorageDir();
        File initializedFile = new File(cacheDir, INITIALIZED_CHECK_FILENAME);
        return initializedFile.isFile();
    }

    /**
     * Create a file, that acts as a "check" and, if present, means
     * that the apps are initialized.
     * @return true if succeeded, false otherwise.
     */
    public boolean setInitialized() {
        File cacheDir = StorageManager.getInstance().getStorageDir();
        File initializedFile = new File(cacheDir, INITIALIZED_CHECK_FILENAME);

        // If already present
        if (initializedFile.isFile())
            return true;

        // Create the empty file
        try {
            initializedFile.createNewFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
