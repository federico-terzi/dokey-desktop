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

    data class Result(val commands: List<Command>,          // The list of commands imported correctly
                      val failed: List<Command>,            // The list of commands that cannot be imported
                      val newIdMapping: Map<Int, Int>)      // The association between the old ids and the new ones

    fun import(sourceFile: File) : Result {
        sourceFile.inputStream().use { stream ->
            val tokener = JSONTokener(stream)
            val json = JSONObject(tokener)

            return import(json)
        }
    }

    fun import(json: JSONObject) : Result {
        val commands = mutableListOf<Command>()
        val failed = mutableListOf<Command>()
        val newIdMapping = mutableMapOf<Int, Int>()

        // Extract the commands fromt the JSON
        val extractedCommands = extractCommandsFromExportJSON(json)

        // Add each command to the system
        extractedCommands.forEach {extractedCommand ->
            // Verify that the command is valid
            if (commandValidator.validate(extractedCommand)) {
                val previousId: Int = extractedCommand.id!!

                // Add the command to the general store
                val command = commandManager.addCommand(extractedCommand)

                // If the command is deleted, make it visible again
                command as CommandWrapper
                if (command.deleted) {
                    command.deleted = false
                    commandManager.saveCommand(command)
                }

                commands.add(command)

                newIdMapping[previousId] = command.id!!
            }else{
                LOG.warning("Cannot import command: ${extractedCommand}")
                failed.add(extractedCommand)
            }
        }

        return Result(commands, failed, newIdMapping)
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