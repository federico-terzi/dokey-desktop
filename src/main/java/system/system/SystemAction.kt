package system.system

enum class SystemAction(val actionId: String, val actionLabel: String) {
    SHUTDOWN("shutdown", "Shutdown"),
    RESTART("restart", "Restart"),
    SUSPEND("suspend", "Suspend"),
    LOGOUT("logout", "Logout"),
    NEXT_TRACK("next_track", "Next Track"),
    PREV_TRACK("prev_track", "Previous Track"),
    PLAY_OR_PAUSE("play_or_pause", "Play or Pause"),
    VOLUME_DOWN("volume_down", "Volume Down"),
    VOLUME_UP("volume_up", "Volume Up"),
    VOLUME_MUTE("volume_mute", "Volume Mute");

    override fun toString(): String {
        return actionLabel
    }

    companion object {
        fun find(id: String?) : SystemAction? {
            return values().find { it.actionId == id }
        }
    }
}