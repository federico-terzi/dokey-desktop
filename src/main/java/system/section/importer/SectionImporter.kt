package system.section.importer

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.Component
import model.parser.section.SectionParser
import model.section.Section
import system.commands.importer.CommandImporter
import system.exceptions.IncompatibleOsException
import system.section.SectionManager
import utils.OSValidator
import java.io.File

class SectionImporter(val sectionManager: SectionManager, val sectionParser: SectionParser,
                      val commandImporter: CommandImporter) {
    fun import(sourceFile: File): Section? {
        sourceFile.inputStream().use { stream ->
            val tokener = JSONTokener(stream)
            val json = JSONObject(tokener)

            return import(json)
        }
    }

    fun import(json: JSONObject): Section? {
        // TODO: check if the section overwrites another one

        // Check if the OS is compatible
        val os = json.getString("os")
        if (os != "any" && os != OSValidator.TAG) {
            throw IncompatibleOsException()
        }

        // Load command metadata
        val commandJson = json.getJSONObject("commands")
        val rawCommands = commandImporter.extractCommandsFromExportJSON(commandJson)
        // Create a map that associates raw command id to a command, needed to later put pieces together
        val oldIdCommandMap = mutableMapOf<Int, Command>()
        rawCommands.forEach { command ->
            oldIdCommandMap[command.id!!] = command
        }

        // Now add all the commands to the system
        val commands = commandImporter.import(commandJson)
        // Create a map that associates the content hash to a list of compatible commands
        val hashCommandMap = mutableMapOf<Int, MutableList<Command>>()
        commands.forEach { command ->
            val hash = command.contentHash()
            if (hashCommandMap[hash] == null) {
                hashCommandMap[hash] = mutableListOf(command)
            } else {
                hashCommandMap[hash]!!.add(command)
            }
        }

        // Parse the section
        val section = sectionParser.fromJSON(json.getJSONObject("section"))

        // This list will hold all the commands that cannot be imported
        val toBeDeleted = mutableListOf<Component>()

        // Correct all the section command references
        section.pages?.forEach { page ->
            for (component in page.components ?: emptyList<Component>()) {
                // Get the reference to the old command
                val oldCommand = oldIdCommandMap[component.commandId]
                if (oldCommand == null) {
                    toBeDeleted.add(component)
                    continue
                }

                // Calculate the hash of the old command and use it to find the compatible new commands
                val oldHash = oldCommand.contentHash()
                val newCommand: Command? = hashCommandMap[oldHash]?.find { it.contentEquals(oldCommand) }

                // If the command is not valid anymore, delete the component
                if (newCommand == null) {
                    toBeDeleted.add(component)
                    continue
                }

                // Replace the old reference with the new one
                component.commandId = newCommand.id
            }
        }

        // Delete components that could not be imported
        section.pages?.forEach { page ->
            page.components?.removeAll(toBeDeleted)
        }

        // Save the section
        if (sectionManager.saveSection(section)) {
            return section
        }

        return null
    }
}