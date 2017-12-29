package system.model;

import system.model.Window;

import java.io.File;
import java.util.List;

public interface ApplicationManager {
    // Name of the cache directory created in the home of the user
    String CACHE_DIRECTORY_NAME = ".remotekey";
    String ICON_CACHE_DIRECTORY_NAME = "icons";

    /**
     * @return the Window object of the active system.window.
     */
    Window getActiveWindow();

    /**
     * @return the PID of the active system.window.
     */
    int getActivePID();

    /**
     * @return the active Application
     */
    Application getActiveApplication();

    /**
     * Get a list of Window currently active.
     */
    List<Window> getWindowList();

    /**
     * Load the Application(s) installed in the system.
     */
    void loadApplications(OnLoadApplicationsListener listener);

    /**
     * @return the list of Application(s) installed in the system.
     */
    List<Application> getApplicationList();

    /**
     * Get the application associated with the given executable path.
     * @param executablePath the path to the application.
     * @return the Application associated with the executable path if found, null otherwise.
     */
    Application getApplication(String executablePath);

    /**
     * Focus an application if already open or start it if not.
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    boolean openApplication(String executablePath);

    /**
     * Return the icon file associated with the specified application.
     * @param executablePath path to the application
     * @return the icon image File object.
     */
    File getApplicationIcon(String executablePath);

    /**
     * Interface used to update the status of the "loadApplications" operation.
     */
    interface OnLoadApplicationsListener {
        void onProgressUpdate(String applicationName, String iconPath, int current, int total);
        void onApplicationsLoaded();
    }
}
