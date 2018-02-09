import system.MS.MSIconExtractor;
import system.SystemManager;
import system.SystemManagerFactory;

public class SystemTestMain {
    public static void main(String[] args) {
        SystemManager systemManager = SystemManagerFactory.getInstance();

        systemManager.logout();

        System.exit(0);
    }
}