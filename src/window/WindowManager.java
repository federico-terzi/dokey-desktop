package window;

public interface WindowManager {
    /**
     * @return the Window object of the active window.
     */
    Window getActiveWindow();

    /**
     * @return the PID of the active window.
     */
    int getActivePID();
}
