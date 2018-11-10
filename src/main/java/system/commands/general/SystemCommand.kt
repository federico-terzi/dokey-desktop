package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "System Action", iconId = "asset:SHUTDOWN")
class SystemCommand : SimpleCommandWrapper() {
    init {
        category = "system"
    }

    var actionType : String?
        get() = this.value
        set(actionType) {
            value = actionType
        }
}