package system.commands.loader

import model.command.Command
import system.commands.annotations.RegisterLoader
import system.commands.general.AppOpenCommand
import system.context.CommandTemplateContext

@RegisterLoader
class ApplicationOpenCommandLoader(val context: CommandTemplateContext) : CommandLoader {
    override fun getTemplateCommands(): List<Command> {
        val commands = mutableListOf<Command>()

        for (application in context.applicationManager.applicationList) {
            val command = AppOpenCommand()
            command.iconId = "app:${application.id}"
            command.appId = application.id
            command.description = "Open ${application.name}"
            command.title = application.name
            command.implicit = true
            command.locked = true
            commands.add(command)
        }

        return commands
    }
}