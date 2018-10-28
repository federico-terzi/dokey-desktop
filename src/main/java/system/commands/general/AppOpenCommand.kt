package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "Launch Application", iconId = "asset:external-link")
class AppOpenCommand : SimpleCommandWrapper() {
    init {
        category = "ao"  // App Open
    }

    var appId : String?
        get() = this.value
        set(appId) {
            value = appId
        }
}