package system.commands.validator.agent

import model.command.Command
import system.commands.general.AppOpenCommand
import system.commands.general.FileOpenCommand
import system.commands.general.KeyboardShortcutCommand
import system.commands.validator.CommandValidationContext
import system.commands.validator.annotation.RegisterValidationAgent
import java.io.File

@RegisterValidationAgent(commandType = KeyboardShortcutCommand::class)
class KeyboardShortcutValidationAgent(val context: CommandValidationContext) : ValidationAgent {
    override fun analyze(command: Command): Boolean {
        command as KeyboardShortcutCommand

        // If the shortcut is associate with an app, make sure the application exists
        if (command.app != null) {
            return context.applicationManager.getApplication(command.app) != null
        }

        return true
    }

    override fun tryToFix(command: Command): Boolean {
        command as KeyboardShortcutCommand

        if (command.app != null) {
            val newExecutablePath = context.applicationPathResolver.searchApp(command.app)

            if (newExecutablePath != null) {
                command.app = newExecutablePath
                return true
            }
        }

        return false
    }
}