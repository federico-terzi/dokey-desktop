package system.commands.importer

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.parser.command.CommandParser
import system.commands.CommandManager
import system.commands.model.CommandWrapper
import system.commands.validator.CommandValidator
import system.exceptions.IncompatibleOsException
import utils.OSValidator
import java.io.File
import java.util.logging.Logger

class CommandImporter(val commandValidator: CommandValidator, val commandParser: CommandParser,
                      val commandManager: CommandManager) {
    fun import(sourceFile: File) : List<Command> {
        sourceFile.inputStream().use { stream ->
            val tokener = JSONTokener(stream)
            val json = JSONObject(tokener)

            return import(json)
        }
    }

    fun import(json: JSONObject) : List<Command> {
        val output = mutableListOf<Command>()

        // Extract the commands fromt the JSON
        val extractedCommands = extractCommandsFromExportJSON(json)

        // Add each command to the system
        extractedCommands.forEach {extractedCommand ->
            // Verify that the command is valid
            if (commandValidator.validate(extractedCommand)) {
                // Add the command to the general store
                val command = commandManager.addCommand(extractedCommand)

                // If the command is deleted, make it visible again
                command as CommandWrapper
                if (command.deleted) {
                    command.deleted = false
                    commandManager.saveCommand(command)
                }

                output.add(command)
            }else{
                LOG.warning("Cannot import command: ${extractedCommand}")
            }
        }

        return output
    }

    fun extractCommandsFromExportJSON(json: JSONObject) : List<Command> {
        val output = mutableListOf<Command>()

        // Check if the OS is compatible
        val os = json.getString("os")
        if (os != "any" && os != OSValidator.TAG) {
            throw IncompatibleOsException()
        }

        val commandsJson = json.getJSONArray("commands")
        for (jsonCommand in commandsJson) {
            jsonCommand as JSONObject

            try {
                val command = commandParser.fromJSON(jsonCommand)
                output.add(command)
            }catch (ex: Exception) {
                LOG.warning("Unable to import command: ${jsonCommand} with exception: ${ex.message}")
            }
        }

        return output
    }

    companion object {
        val LOG = Logger.getGlobal()
    }
}