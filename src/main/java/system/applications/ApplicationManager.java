package system.applications;

import system.storage.StorageManager;

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
     * Open the specified file or folder in the file explorer of the current OS.
     * @param filePath the path to the file/folder
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean open(String filePath);

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

    protected StorageManager storageManager;

    public ApplicationManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Check if the applications have already been initialized.
     * @return true if initialized, false otherwise.
     */
    public boolean isInitialized() {
        File cacheDir = storageManager.getCacheDir();
        File initializedFile = new File(cacheDir, INITIALIZED_CHECK_FILENAME);
        return initializedFile.isFile();
    }

    /**
     * Create a file, that acts as a "check" and, if present, means
     * that the apps are initialized.
     * @return true if succeeded, false otherwise.
     */
    public boolean setInitialized() {
        File cacheDir = storageManager.getCacheDir();
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
