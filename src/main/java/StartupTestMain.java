import system.StartupManager;
import system.SystemManager;
import system.SystemManagerFactory;

public class StartupTestMain {
    public static void main(String[] args) {
        StartupManager startupManager = StartupManager.getInstance();

        int pid = startupManager.getPID();

        String executablePath = startupManager.getExecutablePath(pid);

        boolean isBundle = startupManager.isBundledInstance();

        startupManager.enableAutomaticStartup();

        boolean isEnabled = startupManager.isAutomaticStartupEnabled();

        startupManager.disableAutomaticStartup();

        boolean isEnabled2 = startupManager.isAutomaticStartupEnabled();

        System.exit(0);
    }
}