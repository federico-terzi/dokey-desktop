package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "Launch Application", iconId = "asset:external-link")
class AppOpenCommand : SimpleCommandWrapper() {
    init {
        category = "ao"  // App Open
    }

    var executablePath : String?
        get() = this.value
        set(executablePath) {
            value = executablePath
        }
}