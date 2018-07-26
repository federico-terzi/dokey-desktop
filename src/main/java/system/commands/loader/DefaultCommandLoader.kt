package system.commands.loader

import json.JSONArray
import json.JSONObject
import json.JSONTokener
import model.command.Command
import system.ResourceUtils
import system.commands.annotations.RegisterLoader
import system.commands.general.AppRelatedCommand
import system.context.CommandTemplateContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

@RegisterLoader
class DefaultCommandLoader(val context: CommandTemplateContext) : CommandLoader {
    override fun getTemplateCommands(): List<Command> {
        // Get the template file
        val templateFile = ResourceUtils.getResource("/commands/defaults.json")

        val commandParser = context.commandParser
        val fis = FileInputStream(templateFile)
        val tokener = JSONTokener(fis)
        val jsonArray = JSONArray(tokener)

        val commands = mutableListOf<Command>()

        for (jsonObj in jsonArray) {
            val command = commandParser.fromJSON(jsonObj as JSONObject)

            commands.add(command)
        }

        return commands
    }
}