package system.commands.specific

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper
import system.external.photoshop.PhotoshopCommandAction

@RegisterCommand(title = "Photoshop Command", iconId = "asset:sliders")
class PhotoshopCommand : SimpleCommandWrapper() {
    init {
        category = "photoshopcmd"
    }

    var command : PhotoshopCommandAction?
        get() = PhotoshopCommandAction.find(this.value)
        set(actionType) {
            this.value = actionType?.actionId
        }
}