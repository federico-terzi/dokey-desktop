package system

import system.model.Application
import system.model.ApplicationManager

/**
 * Used to check periodically which app is in focus.
 */
class ApplicationSwitchDaemon(val appManager : ApplicationManager) : Thread(){

    companion object {
        val DEFAULT_CHECK_INTERVAL : Long = 500 // How ofter check for app changes ( in milliseconds )
    }

    var checkInterval : Long = DEFAULT_CHECK_INTERVAL
    var shouldStop = false

    val listeners : MutableList<OnApplicationSwitchListener> = mutableListOf()

    override fun run() {
        var previousPID = appManager.activePID

        while (!shouldStop) {
            val currentPID = appManager.activePID

            // Check for changes in the active pid
            if (currentPID != previousPID) {  // APP CHANGED
                // Get the active application
                val application = appManager.activeApplication

                // Make sure the application exists
                if (application != null) {
                    println(application)

                    // Notify the change to all listeners
                    listeners.forEach({
                        try {
                            it.onApplicationSwitch(application)
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
