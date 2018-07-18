package system.commands.general

import system.commands.annotations.ExecutableCommand

@ExecutableCommand
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