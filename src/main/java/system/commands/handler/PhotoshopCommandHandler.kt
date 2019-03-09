package system.commands.handler

import system.commands.annotations.RegisterHandler
import system.commands.specific.PhotoshopCommand
import system.context.GeneralContext

@RegisterHandler(commandType = PhotoshopCommand::class)
class PhotoshopCommandHandler(context: GeneralContext) : CommandHandler<PhotoshopCommand>(context) {
    override fun handleInternal(command: PhotoshopCommand) {
        context.photoshopManager.executeCommand(command.command!!)
    }
}