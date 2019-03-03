package system.commands.handler

import system.commands.annotations.RegisterHandler
import system.commands.specific.PhotoshopSliderCommand
import system.context.GeneralContext

@RegisterHandler(commandType = PhotoshopSliderCommand::class)
class PhotoshopSliderCommandHandler(context: GeneralContext) : CommandHandler<PhotoshopSliderCommand>(context) {
    override fun handleInternal(command: PhotoshopSliderCommand) {
        context.photoshopManager.moveSlider(command.sliderId!!, command.value!!.toDouble())
    }
}