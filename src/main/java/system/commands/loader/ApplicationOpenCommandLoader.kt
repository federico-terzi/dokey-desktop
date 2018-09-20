package system.commands.loader

import json.JSONArray
import json.JSONObject
import json.JSONTokener
import model.command.Command
import system.ResourceUtils
import system.commands.annotations.RegisterLoader
import system.commands.general.AppOpenCommand
import system.commands.general.AppRelatedCommand
import system.context.CommandTemplateContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

@RegisterLoader
class ApplicationOpenCommandLoader(val context: CommandTemplateContext) : CommandLoader {
    override fun getTemplateCommands(): List<Command> {
        val commands = mutableListOf<Command>()

        for (application in context.applicationManager.applicationList) {
            val command = AppOpenCommand()
            command.iconId = "app:${application.executablePath}"
            command.executablePath = application.executablePath
            command.description = "Open ${application.name}"
            command.title = application.name
            command.implicit = true
            command.locked = true
            commands.add(command)
        }

        return commands
    }
}