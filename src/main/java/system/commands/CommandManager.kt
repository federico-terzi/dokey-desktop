package system.commands

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.CommandResolver
import model.parser.command.CommandParser
import system.commands.general.AppRelatedCommand
import system.storage.StorageManager
import java.io.*
import java.util.logging.Logger


class CommandManager(val commandParser: CommandParser, val storageManager: StorageManager,
                     val templateLoader: CommandTemplateLoader) : CommandResolver {
    // Load the command directory, where all the command files are saved
    val commandDir = storageManager.commandDir

    // This structure will hold all the commands, associated with their IDs
    val commandMap = mutableMapOf<Int, Command>()

    val LOG = Logger.getGlobal()

    /**
     * Load all the commands.
     * It loads all the user commands and template commands, then it compares them
     * to inject the compatible templates that are not already present.
     * NOTE: it must be called after the application manager has been initialized.
     */
    fun initialize() {
        var (userCommands, maxId) = loadCommands()
        val templateCommands = templateLoader.getTemplateCommands()

        LOG.info("Loaded ${userCommands.size} user commands, joining with ${templateCommands.size} templates...")

        val conflictMap = mutableMapOf<Int, MutableList<Command>>()
        userCommands.forEach { command ->
            val hash = command.contentHash()
            var conflicting = false

            if (conflictMap[hash] != null && conflictMap[hash]!!.any { it.contentEquals(command) }) {
                conflicting = true
            }

            if (conflictMap[hash] == null) {
                conflictMap[hash] = mutableListOf(command)
            }else{
                conflictMap[hash]!!.add(command)
            }

            if (!conflicting) {
                commandMap[command.id!!] = command
            }
        }

        templateCommands.forEach { template ->
            val hash = template.contentHash()
            var conflicting = false

            if (conflictMap[hash] != null && conflictMap[hash]!!.any { it.contentEquals(template) }) {
                conflicting = true
            }else{
                if (conflictMap[hash] == null) {
                    conflictMap[hash] = mutableListOf(template)
                }else{
                    conflictMap[hash]!!.add(template)
                }
            }

            if (!conflicting) {
                template.id = ++maxId
                commandMap[maxId] = template
                saveCommand(template)
            }
        }
    }



    /**
     * Load all the saved commands
     */
    private fun loadCommands() : Pair<List<Command>, Int> {
        val commands = mutableListOf<Command>()

        var maxId = 0

        for (file in commandDir.listFiles()) {
            val command = readCommandFromFile(file)
            commands.add(command)

            if (command.id!! > maxId) {
                maxId = command.id!!
            }
        }

        return Pair(commands, maxId)
    }

    @Synchronized
    fun saveCommand(command: Command) : Boolean {
        val destFile = File(storageManager.commandDir, "${command.id}.json")
        command.lastEdit = System.currentTimeMillis()
        return writeCommandToFile(command, destFile)
    }

    @Synchronized
    private fun writeCommandToFile(command: Command, dest: File) : Boolean {
        try {
            // Write the json command to the file
            val fos = FileOutputStream(dest)
            val pw = PrintWriter(fos)
            val commandJson = command.json()
            pw.write(commandJson.toString())
            pw.close()

            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        return false
    }

    /**
     * Read and parse the command from the specified JSON file
     */
    private fun readCommandFromFile(commandFile : File) : Command {
        val commandParser = commandParser
        val fis = FileInputStream(commandFile)
        val tokener = JSONTokener(fis)
        val jsonContent = JSONObject(tokener)
        return commandParser.fromJSON(jsonContent)
    }

    override fun getCommand(id: Int): Command? {
        return commandMap[id]
    }

    /**
     * Return all the commands t
     */
    fun searchCommands(query : String? = null, limit: Int = 0) : Collection<Command> {
        if (query == null) {
            return commandMap.values
        }else{
            val filteringFunction : (Command) -> Boolean = {
                        it.title?.contains(query, true) ?: false ||
                        it.description?.contains(query, true) ?: false
            }
            if (limit > 0) {
                return commandMap.values.filter(filteringFunction).take(limit)
            }else{
                return commandMap.values.filter(filteringFunction)
            }
        }
    }

    fun getAppRelatedCommands() : Collection<AppRelatedCommand> {
        return commandMap.values.filter { it is AppRelatedCommand }.map { it as AppRelatedCommand }.filter { it.app != null }
    }

    fun getCommandsForApp(appPath: String) : Collection<AppRelatedCommand> {
        return getAppRelatedCommands().filter { it.app == appPath }
    }
}