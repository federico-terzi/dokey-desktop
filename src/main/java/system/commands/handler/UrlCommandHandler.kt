package system.commands.handler

import system.commands.annotations.RegisterHandler
import system.commands.general.FolderOpenCommand
import system.commands.general.UrlCommand
import system.context.GeneralContext

@RegisterHandler(commandType = UrlCommand::class)
class UrlCommandHandler(context: GeneralContext) : CommandHandler<UrlCommand>(context) {
    override fun handleInternal(command: UrlCommand) {
        context.applicationManager.openWebLink(command.url)
    }
}