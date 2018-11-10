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
     * Execute the system action specified by the string
     * @return true if succeeded, false if an error occurred.
     */
    fun execute(action: String?): Boolean {
        return when (action) {
            "shutdown" -> shutdownPC()
            "restart" -> restart()
            "suspend" -> suspend()
            "logout" -> logout()
            "next_track" -> nextTrack()
            "prev_track" -> previousTrack()
            "play_or_pause" -> playOrPause()
            "volume_down" -> volumeDown()
            "volume_up" -> volumeUp()
            "volume_mute" -> volumeMute()
            else -> false
        }
    }
}
