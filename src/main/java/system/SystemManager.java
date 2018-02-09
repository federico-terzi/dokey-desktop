package system;

/**
 * This interface can be used to dispatch some system specific commands
 * like shutdown, volume, ecc.
 */
public interface SystemManager {

    // SYSTEM COMMANDS

    /**
     * Shutdown the computer.
     * @return true if succeeded, false if an error occurred.
     */
    boolean shutdown();

    /**
     * Restart the computer.
     * @return true if succeeded, false if an error occurred.
     */
    boolean restart();

    /**
     * Log out the user from the computer.
     * @return true if succeeded, false if an error occurred.
     */
    boolean logout();

    /**
     * Suspend the computer.
     * @return true if succeeded, false if an error occurred.
     */
    boolean suspend();

    // MEDIA COMMANDS

    /**
     * Turn down the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    boolean volumeDown();

    /**
     * Turn up the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    boolean volumeUp();

    /**
     * Mute the sound volume.
     * @return true if succeeded, false if an error occurred.
     */
    boolean volumeMute();

    /**
     * Send the play/pause command.
     * @return true if succeeded, false if an error occurred.
     */
    boolean playOrPause();

    /**
     * Go to the next track.
     * @return true if succeeded, false if an error occurred.
     */
    boolean nextTrack();

    /**
     * Go to the previous track.
     * @return true if succeeded, false if an error occurred.
     */
    boolean previousTrack();
}
