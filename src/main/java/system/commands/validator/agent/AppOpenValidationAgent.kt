package system.commands.validator.agent

import model.command.Command
import system.commands.general.AppOpenCommand
import system.commands.general.FileOpenCommand
import system.commands.validator.CommandValidationContext
import system.commands.validator.annotation.RegisterValidationAgent
import java.io.File

@RegisterValidationAgent(commandType = AppOpenCommand::class)
class AppOpenValidationAgent(val context: CommandValidationContext) : ValidationAgent {
    override fun analyze(command: Command): Boolean {
        command as AppOpenCommand

        // Check if the application exists
        return context.applicationManager.getApplication(command.executablePath) != null
    }

    override fun tryToFix(command: Command): Boolean {
        command as AppOpenCommand

        val newExecutablePath = context.applicationPathResolver.searchApp(command.executablePath)

        if (newExecutablePath != null) {
            command.executablePath = newExecutablePath
            return true
        }

        return false
    }
}