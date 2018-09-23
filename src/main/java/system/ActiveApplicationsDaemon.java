package system;

import system.applications.Application;
import system.applications.ApplicationManager;

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
    private List<Application> activeApplications = new ArrayList<>();

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public ActiveApplicationsDaemon(ApplicationManager appManager, DaemonMonitor daemonMonitor) {
        this.appManager = appManager;
        this.daemonMonitor = daemonMonitor;

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
                // Get the currently active apps
                activeApplications = appManager.getActiveApplications();

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
