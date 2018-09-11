package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand
class AppOpenCommand : SimpleCommand() {
    init {
        category = "ao"  // App Open
    }

    var executablePath : String?
        get() = this.value
        set(executablePath) {
            value = executablePath
        }
}