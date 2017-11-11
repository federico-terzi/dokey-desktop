package system.model;

import system.model.Window;

import java.util.List;

public interface ApplicationManager {
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
     * Return the list of Application(s) installed in the system.
     */
    List<Application> getApplicationList();

    /**
     * Interface used to update the status of the "loadApplications" operation.
     */
    interface OnLoadApplicationsListener {
        void onProgressUpdate(String applicationName, int current, int total);
        void onApplicationsLoaded();
    }
}
