package system;

import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Daemon used to keep track of the active applications in the system.
 */
public class ActiveApplicationsDaemon extends Thread{

    public static final long CHECK_INTERVAL = 1000;  // Check interval

    private volatile boolean shouldStop = false;
    private ApplicationManager appManager;
    private DaemonMonitor daemonMonitor;

    // The synchronized list that will hold the currently active apps
    private List<Application> activeApplications = Collections.synchronizedList(new ArrayList<>(100));

    // The list of apps that must be filtered out
    private Set<String> skippedApps = new HashSet<>();

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public ActiveApplicationsDaemon(ApplicationManager appManager, DaemonMonitor daemonMonitor) {
        this.appManager = appManager;
        this.daemonMonitor = daemonMonitor;

        // Initialize the apps that will be filtered out
        skippedApps.add("WinStore.App.exe");
        skippedApps.add("Microsoft.Photos.exe");
        skippedApps.add("ApplicationFrameHost.exe");
        skippedApps.add("ShellExperienceHost.exe");
        skippedApps.add("googledrivesync.exe");
        skippedApps.add("SearchUI.exe");
        skippedApps.add("SystemSettings.exe");
        skippedApps.add("SystemSettingsBroker.exe");
        skippedApps.add("Calculator.exe");

        setName("Active Applications Daemon");
    }

    @Override
    public void run() {
        while (!shouldStop) {
            // Check if thread should be paused
            daemonMonitor.getLock().lock();
            try {
                while(daemonMonitor.shouldPause()) {
                    LOG.info("ACTIVE APPLICATIONS DAEMON PAUSED");
                    // If the thread should be paused, block it until a new device is available
                    daemonMonitor.getNotPaused().await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                daemonMonitor.getLock().unlock();
            }

            try {
                // Get the currently active apps, filtering out the skipped ones
                List<Application> currentlyActive = appManager.getActiveApplications().stream().filter(
                        (application -> {
                            File appFile = new File(application.getExecutablePath());
                            return !skippedApps.contains(appFile.getName());
                        })
                ).collect(Collectors.toList());

                // Add the app that are not yet present
                for (Application app : currentlyActive) {
                    if (!activeApplications.contains(app)) {
                        activeApplications.add(app);
                    }
                }

                // Remove those who are not present anymore
                List<Application> toBeDeleted = new LinkedList<>();
                for (Application app : activeApplications) {
                    if (!currentlyActive.contains(app)) {
                        toBeDeleted.add(app);
                    }
                }
                activeApplications.removeAll(toBeDeleted);

                Thread.sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop the daemon.
     */
    public void stopDaemon() {
        shouldStop = true;
    }

    public List<Application> getActiveApplications() {
        return activeApplications;
    }
}
