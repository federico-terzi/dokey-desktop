package system.commands.validator.agent

import model.command.Command
import system.commands.general.AppOpenCommand
import system.commands.validator.CommandValidationContext
import system.commands.validator.annotation.RegisterValidationAgent

@RegisterValidationAgent(commandType = AppOpenCommand::class)
class AppOpenValidationAgent(val context: CommandValidationContext) : ValidationAgent {
    override fun analyze(command: Command): Boolean {
        command as AppOpenCommand

        // Check if the application exists
        return context.applicationManager.getApplication(command.appId) != null
    }

    override fun tryToFix(command: Command): Boolean {
        command as AppOpenCommand

        val newExecutablePath = context.applicationPathResolver.searchApp(command.appId)

        if (newExecutablePath != null) {
            command.appId = newExecutablePath
            return true
        }

        return false
    }
}