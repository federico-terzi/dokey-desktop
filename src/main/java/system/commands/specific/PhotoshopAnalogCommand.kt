package system.commands.specific

import system.commands.annotations.RegisterCommand
import system.commands.model.AnalogCommandWrapper

@RegisterCommand(title = "Photoshop Slider", iconId = "asset:folder")
class PhotoshopAnalogCommand : AnalogCommandWrapper() {
    init {
        category = "photoshop"
    }

    var sliderId : String? // Identifier of the slider, used to find the corresponding action
        get() = this.param
        set(_sliderId) {
            this.param = _sliderId
        }
}