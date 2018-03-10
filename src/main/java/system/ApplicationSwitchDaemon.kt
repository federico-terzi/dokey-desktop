package system

import system.model.Application
import system.model.ApplicationManager
import java.util.logging.Logger

/**
 * Used to check periodically which app is in focus.
 */
class ApplicationSwitchDaemon(val appManager : ApplicationManager, val daemonMonitor: DaemonMonitor) : Thread(){

    companion object {
        val DEFAULT_CHECK_INTERVAL : Long = 500 // How ofter check for app changes ( in milliseconds )
    }

    init {
        name = "Application Switch Daemon"
    }

    var checkInterval : Long = DEFAULT_CHECK_INTERVAL
    var shouldStop = false

    var currentApplication : Application? = null  // The currently active application

    val listeners : MutableList<OnApplicationSwitchListener> = mutableListOf()

    val LOG : Logger = Logger.getGlobal()

    override fun run() {
        var previousPID = -1

        while (!shouldStop) {
            // Check if thread should be paused
            daemonMonitor.lock.lock()
            try {
                while (daemonMonitor.shouldPause()) {
                    LOG.info("APPLICATION SWITCH DAEMON PAUSED")
                    // If the thread should be paused, block it until a new device is available
                    daemonMonitor.notPaused.await()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                daemonMonitor.lock.unlock()
            }

            val currentPID = appManager.activePID

            // Check for changes in the active pid
            if (currentPID != previousPID) {  // APP CHANGED
                // Get the active application
                currentApplication = appManager.activeApplication

                // Make sure the application exists
                if (currentApplication != null) {
                    // Notify the change to all listeners
                    listeners.forEach({
                        try {
                            it.onApplicationSwitch(currentApplication!!)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })
                }
            }

            previousPID = currentPID

            Thread.sleep(checkInterval)
        }
    }

    /**
     * Add a listener
     */
    fun addApplicationSwitchListener(listener : OnApplicationSwitchListener) {
        listeners.add(listener)
    }

    /**
     * Remove the listener
     */
    fun removeApplicationSwitchListener(listener: OnApplicationSwitchListener) {
        listeners.remove(listener)
    }

    /**
     * Interface used for the callback
     */
    interface OnApplicationSwitchListener {
        fun onApplicationSwitch(application: Application)
    }
}
