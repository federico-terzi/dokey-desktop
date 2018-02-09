package system;

import system.MAC.MACApplicationManager;
import system.MS.MSApplicationManager;
import system.MS.MSSystemManager;
import system.model.ApplicationManager;
import utils.OSValidator;

public class SystemManagerFactory {
    private static SystemManager instance = null;

    /**
     * @return the correct SystemManager based on the OS.
     */
    public static SystemManager getInstance() {
        if (instance == null) {
            // Decide the manager based on the OS
            if (OSValidator.isWindows()) {  // WINDOWS
                instance = new MSSystemManager();
            }else if (OSValidator.isMac()) {  // MAC OSX
                // TODO
            }
        }
        return instance;
    }
}
