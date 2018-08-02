package system.commands.handler

import system.context.GeneralContext
import system.commands.annotations.RegisterHandler
import system.commands.general.FileOpenCommand
import system.commands.general.FolderOpenCommand

@RegisterHandler(commandType = FileOpenCommand::class)
class FileOpenCommandHandler(context: GeneralContext) : CommandHandler<FileOpenCommand>(context) {
    override fun handleInternal(command: FileOpenCommand) {
        context.applicationManager.open(command.file)
    }
}