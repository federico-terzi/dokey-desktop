package system;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * This class is used to pause and wake up daemons when they are not used.
 */
public class DaemonMonitor {
    private Lock lock = new ReentrantLock();

    private Condition notPaused = lock.newCondition();  // When this condition is met, all the daemon threads restart.

    private volatile boolean shouldPause = true;  // If this is true, all dependent daemons will pause.

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    /**
     * Pause all the dependent Daemons until wakeUp() is called.
     */
    public void pause() {
        LOG.info("Pausing daemons...");
        shouldPause = true;
    }

    /**
     * Restart all the previously paused dependent Daemons.
     */
    public void wakeUp() {
        LOG.info("Waking up all daemons...");

        lock.lock();
        try {
            shouldPause = false;
            notPaused.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public Lock getLock() {
        return lock;
    }

    public boolean shouldPause() {
        return shouldPause;
    }

    public Condition getNotPaused() {
        return notPaused;
    }
}
