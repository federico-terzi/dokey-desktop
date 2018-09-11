package system.commands.handler

import system.context.GeneralContext
import system.commands.annotations.RegisterHandler
import system.commands.general.AppOpenCommand

@RegisterHandler(commandType = AppOpenCommand::class)
class AppOpenCommandHandler(context: GeneralContext) : CommandHandler<AppOpenCommand>(context) {
    override fun handleInternal(command: AppOpenCommand) {
        // Focus or open the app
        command.executablePath?.let {
            Thread {
                context.applicationManager.openApplication(it)
            }.start()
        }
    }
}