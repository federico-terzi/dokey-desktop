package system.system

/**
 * This interface can be used to dispatch some system specific commands
 * like shutdownPC, volume, ecc.
 */
abstract class SystemManager {
    // MEDIA COMMANDS

    /**
     * Turn down the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun volumeDown(): Boolean

    /**
     * Turn up the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun volumeUp(): Boolean

    /**
     * Mute the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun volumeMute(): Boolean

    /**
     * Send the play/pause command.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun playOrPause(): Boolean

    /**
     * Go to the next track.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun nextTrack(): Boolean

    /**
     * Go to the previous track.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun previousTrack(): Boolean

    // SYSTEM COMMANDS

    /**
     * Shutdown the computer.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun shutdownPC(): Boolean

    /**
     * Restart the computer.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun restart(): Boolean

    /**
     * Log out the user from the computer.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun logout(): Boolean

    /**
     * Suspend the computer.
     * @return true if succeeded, false if an error occurred.
     */
    abstract fun suspend(): Boolean

    /**
     * Execute the system action specified by the action type
     * @return true if succeeded, false if an error occurred.
     */
    fun execute(actionEnum: SystemAction?): Boolean {
        when (actionEnum) {
            SystemAction.SHUTDOWN -> shutdownPC()
            SystemAction.RESTART -> restart()
            SystemAction.SUSPEND -> suspend()
            SystemAction.LOGOUT -> logout()
            SystemAction.NEXT_TRACK -> nextTrack()
            SystemAction.PREV_TRACK -> previousTrack()
            SystemAction.PLAY_OR_PAUSE -> playOrPause()
            SystemAction.VOLUME_DOWN -> volumeDown()
            SystemAction.VOLUME_UP -> volumeUp()
            SystemAction.VOLUME_MUTE -> volumeMute()
        }

        return false
    }
}
