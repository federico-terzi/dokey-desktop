package system.section.exporter

import json.JSONObject
import model.command.Command
import model.section.Section
import system.commands.CommandManager
import system.commands.exporter.CommandExporter
import utils.OSValidator
import java.io.File

class SectionExporter(val commandExporter: CommandExporter, val commandManager: CommandManager) {
    fun export(section: Section, destinationFile: File) {
        // Generate the json payload
        val payload = generateExportPayload(section)

        // Save it to file
        destinationFile.printWriter().use {
            it.write(payload.toString())
        }
    }

    private fun generateExportPayload(section: Section) : JSONObject {
        val json = JSONObject()

        // Add metadata
        json.put("os", OSValidator.TAG)

        // Add the section
        json.put("section", section.jsonExport())

        // Add the connected commands
        val commands = mutableListOf<Command>()
        section.pages?.forEach { page ->
            page.components?.forEach { component ->
                if (component.commandId != null) {
                    val command = commandManager.getCommand(component.commandId!!)
                    if (command != null) {
                        commands.add(command)
                    }
                }
            }
        }
        // Generate the command payload
        val commandJson = commandExporter.generateExportPayload(commands)
        json.put("commands", commandJson)

        return json
    }
}