package window;

public class WindowManagerFactory {
    private static WindowManager instance = null;

    /**
     * @return the correct WindowManager based on the OS.
     */
    public static WindowManager getInstance() {
        // TODO: check which window manager should use based on the OS.
        if (instance == null) {
            instance = new MSWindowManager();
        }
        return instance;
    }
}
