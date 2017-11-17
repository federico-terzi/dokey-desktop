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
     * Focus an application if already open or start it if not.
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    boolean openApplication(String executablePath);

    /**
     * @return the Cache directory used to save images and files, must be implemented for each OS.
     */
    File getCacheDir();

    /**
     * @return the icon Cache directory used to save images, must be implemented for each OS.
     */
    File getIconCacheDir();

    /**
     * Interface used to update the status of the "loadApplications" operation.
     */
    interface OnLoadApplicationsListener {
        void onProgressUpdate(String applicationName, String iconPath, int current, int total);
        void onApplicationsLoaded();
    }
}
