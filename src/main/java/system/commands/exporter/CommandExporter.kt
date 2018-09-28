package system.commands.exporter

import json.JSONObject
import json.JSONArray
import model.command.Command
import utils.OSValidator
import java.io.File

class CommandExporter {
    fun exportCommands(commands: List<Command>, destinationFile: File) {
        // Generate the json payload
        val payload = generateExportPayload(commands)

        // Save it to file
        destinationFile.printWriter().use {
            it.write(payload.toString())
        }
    }

    private fun generateExportPayload(commands: List<Command>) : JSONObject {
        val json = JSONObject()

        // Add metadata
        json.put("os", OSValidator.TAG)

        // Add all the commands
        val commandsJson = JSONArray()
        commands.forEach {
            commandsJson.put(it.jsonExport())
        }
        json.put("commands", commandsJson)

        return json
    }
}