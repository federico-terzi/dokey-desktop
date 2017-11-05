package window;

import java.util.List;

public interface WindowManager {
    /**
     * @return the Window object of the active window.
     */
    Window getActiveWindow();

    /**
     * @return the PID of the active window.
     */
    int getActivePID();

    /**
     * Get a list of Windows currently active.
     */
    List<Window> getWindowList();
}
