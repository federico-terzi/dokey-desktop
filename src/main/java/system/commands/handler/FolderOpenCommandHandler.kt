package system.commands.handler

import system.DokeyContext
import system.commands.annotations.RegisterHandler
import system.commands.general.FolderOpenCommand

@RegisterHandler(commandType = FolderOpenCommand::class)
class FolderOpenCommandHandler(context: DokeyContext) : CommandHandler<FolderOpenCommand>(context) {
    override fun handleInternal(command: FolderOpenCommand) {
        context.applicationManager.openFolder(command.folder)
    }
}