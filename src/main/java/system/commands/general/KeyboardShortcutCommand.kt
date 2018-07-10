package system.commands.general

import model.command.SimpleCommand

class KeyboardShortcutCommand : SimpleCommand() {
    init {
        category = "ks"  // Keyboard Shortcut
    }

    var shortcut : String?
        get() = this.value
        set(shortcut) {
            value = shortcut
        }
}