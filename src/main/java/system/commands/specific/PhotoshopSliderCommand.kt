package system.commands.specific

import system.commands.annotations.RegisterCommand
import system.commands.model.AnalogCommandWrapper
import system.external.photoshop.PhotoshopSliderAction

@RegisterCommand(title = "Photoshop Slider", iconId = "asset:sliders")
class PhotoshopSliderCommand : AnalogCommandWrapper() {
    init {
        category = "photoshop"
    }

    var slider : PhotoshopSliderAction?  // Identifier of the slider, used to find the corresponding action
        get() = PhotoshopSliderAction.find(this.param)
        set(actionType) {
            this.param = actionType?.actionId
        }
}