package system.commands.validator.agent

import model.command.Command
import system.commands.general.FileOpenCommand
import system.commands.validator.CommandValidationContext
import system.commands.validator.annotation.RegisterValidationAgent
import java.io.File

@RegisterValidationAgent(commandType = FileOpenCommand::class)
class FileOpenValidationAgent(val context: CommandValidationContext) : ValidationAgent {
    override fun analyze(command: Command): Boolean {
        command as FileOpenCommand

        // Make sure the file exists
        val file = File(command.file)

        return file.isFile
    }

    override fun tryToFix(command: Command): Boolean {
        return false // Cannot fix
    }
}