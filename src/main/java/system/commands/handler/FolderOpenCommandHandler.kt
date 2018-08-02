package system.commands.handler

import system.context.GeneralContext
import system.commands.annotations.RegisterHandler
import system.commands.general.FolderOpenCommand

@RegisterHandler(commandType = FolderOpenCommand::class)
class FolderOpenCommandHandler(context: GeneralContext) : CommandHandler<FolderOpenCommand>(context) {
    override fun handleInternal(command: FolderOpenCommand) {
        context.applicationManager.open(command.folder)
    }
}