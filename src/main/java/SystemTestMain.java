import system.MS.MSIconExtractor;
import system.SystemManager;
import system.SystemManagerFactory;

public class SystemTestMain {
    public static void main(String[] args) {
        SystemManager systemManager = SystemManagerFactory.getInstance();

        systemManager.playOrPause();

        System.exit(0);
    }
}