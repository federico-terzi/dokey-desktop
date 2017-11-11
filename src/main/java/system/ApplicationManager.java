package system;

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
     * Get a list of Windows currently active.
     */
    List<Window> getWindowList();
}
