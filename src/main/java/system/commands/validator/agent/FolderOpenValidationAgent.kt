package system.commands.validator.agent

import model.command.Command
import system.commands.general.FolderOpenCommand
import system.commands.validator.CommandValidationContext
import system.commands.validator.annotation.RegisterValidationAgent
import java.io.File

@RegisterValidationAgent(commandType = FolderOpenCommand::class)
class FolderOpenValidationAgent(val context: CommandValidationContext) : ValidationAgent {
    override fun analyze(command: Command): Boolean {
        command as FolderOpenCommand

        // Make sure the folder exists
        val file = File(command.folder)

        return file.isDirectory
    }

    override fun tryToFix(command: Command): Boolean {
        return false // Cannot fix
    }
}