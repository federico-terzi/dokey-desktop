package system.commands.handler

import system.commands.annotations.RegisterHandler
import system.commands.general.SystemCommand
import system.context.GeneralContext

@RegisterHandler(commandType = SystemCommand::class)
class SystemCommandHandler(context: GeneralContext) : CommandHandler<SystemCommand>(context) {
    override fun handleInternal(command: SystemCommand) {
        context.systemManager.execute(command.actionType)
    }
}