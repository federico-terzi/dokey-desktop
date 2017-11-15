package system;

import system.MAC.MACApplicationManager;
import system.MS.MSApplicationManager;
import system.model.ApplicationManager;
import utils.OSValidator;

public class ApplicationManagerFactory {
    private static ApplicationManager instance = null;

    /**
     * @return the correct ApplicationManager based on the OS.
     */
    public static ApplicationManager getInstance() {
        if (instance == null) {
            // Decide the manager based on the OS
            if (OSValidator.isWindows()) {  // WINDOWS
                instance = new MSApplicationManager();
            }else if (OSValidator.isMac()) {  // MAC OSX
                instance = new MACApplicationManager();
            }
        }
        return instance;
    }
}
