package system.commands.general

class KeyboardShortcutCommand : SimpleAppRelatedCommand() {
    init {
        category = "ks"  // Keyboard Shortcut
    }

    var shortcut : String?
        get() = this.value
        set(shortcut) {
            value = shortcut
        }
}