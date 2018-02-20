package system;

import system.MS.MSStartupManager;
import utils.OSValidator;

/**
 * This manager is used to subscribe the app to the startup mechanism.
 */
public abstract class StartupManager {
    private static StartupManager instance = null;

    // This will be filled with the executable path of the application
    protected String executablePath;

    protected StartupManager() {
        // Get the executable path at startup
        int pid = getPID();
        executablePath = getExecutablePath(pid);
    }

    /**
     * @return the PID of the current process.
     */
    public abstract int getPID();

    /**
     * @param pid the PID of the process
     * @return the executable path of the process, from the given PID.
     */
    public abstract String getExecutablePath(int pid);

    /**
     * Check if the current instance of dokey is bundled ( .exe or .app ) or it's
     * invoked using java.
     * In the latter case, the automatic startup cannot be enabled.
     * @return true if dokey is executed as a bundled instance, false if using pure JAVA.
     */
    public abstract boolean isBundledInstance();

    /**
     * @return true if Dokey is automatically started at startup, false otherwise.
     */
    public abstract boolean isAutomaticStartupEnabled();

    /**
     * Enable the automatic startup of Dokey
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean enableAutomaticStartup();

    /**
     * Disable the automatic startup of Dokey
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean disableAutomaticStartup();

    /**
     * @return the correct StartupManager instance based on the OS.
     */
    public static StartupManager getInstance() {
        if (instance == null) {
            // Decide the manager based on the OS
            if (OSValidator.isWindows()) {  // WINDOWS
                instance = new MSStartupManager();
            }else if (OSValidator.isMac()) {  // MAC OSX
                // TODO instance = new MACSystemManager();
            }
        }
        return instance;
    }
}
