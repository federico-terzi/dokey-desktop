package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper
import system.system.SystemAction

@RegisterCommand(title = "System Action", iconId = "asset:SHUTDOWN")
class SystemCommand : SimpleCommandWrapper() {
    init {
        category = "system"
    }

    var actionType : SystemAction?
        get() = SystemAction.find(this.value)
        set(actionType) {
            value = actionType?.actionId
        }
}