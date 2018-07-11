package system.commands

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.CommandResolver
import model.parser.ModelParser
import system.storage.StorageManager
import java.io.File
import java.io.FileInputStream

class CommandManager(val modelParser: ModelParser, storageManager: StorageManager) : CommandResolver {
    // Load the command directory, where all the command files are saved
    val commandDir = storageManager.commandDir

    // This structure will hold all the commands, associated with their IDs
    val commandMap = mutableMapOf<Int, Command>()

    init {
        // TODO: Add command templates and automatic importing at startup based on the user system ( installed apps, ecc )

        loadCommands()
    }

    /**
     * Load all the saved commands
     */
    private fun loadCommands() {
        for (file in commandDir.listFiles()) {
            val command = readCommandFromFile(file)
            commandMap[command.id!!] = command
        }
    }

    /**
     * Read and parse the command from the specified JSON file
     */
    private fun readCommandFromFile(commandFile : File) : Command {
        val commandParser = modelParser.commandParser
        val fis = FileInputStream(commandFile)
        val tokener = JSONTokener(fis)
        val jsonContent = JSONObject(tokener)
        return commandParser.fromJSON(jsonContent)
    }

    override fun getCommand(id: Int): Command? {
        return commandMap[id]
    }
}