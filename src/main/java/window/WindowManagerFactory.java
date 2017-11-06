package window;

import utils.OSValidator;

public class WindowManagerFactory {
    private static WindowManager instance = null;

    /**
     * @return the correct WindowManager based on the OS.
     */
    public static WindowManager getInstance() {
        if (instance == null) {
            // Decide the manager based on the OS
            if (OSValidator.isWindows()) {  // WINDOWS
                instance = new MSWindowManager();
            }
        }
        return instance;
    }
}
