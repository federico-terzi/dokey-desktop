package system.commands

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.CommandResolver
import model.parser.command.CommandParser
import system.applications.Application
import system.commands.general.AppOpenCommand
import system.commands.general.AppRelatedCommand
import system.commands.model.CommandWrapper
import system.storage.StorageManager
import java.io.*
import java.util.logging.Logger


class CommandManager(val commandParser: CommandParser, val storageManager: StorageManager,
                     val templateLoader: CommandTemplateLoader) : CommandResolver {
    // Load the command directory, where all the command files are saved
    val commandDir = storageManager.commandDir

    // This structure will hold all the commands, associated with their IDs
    val commandMap = mutableMapOf<Int, CommandWrapper>()

    val LOG = Logger.getGlobal()

    // Maximum id of the system, used when adding a new command
    private var maxId : Int = 0

    // Used to detect conflicts between commands, it associates the contentHash
    // of a command to a list of possible instances.
    // This is useful to avoid adding commands equal in content.
    private val conflictMap = mutableMapOf<Int, MutableList<Command>>()

    /**
     * Load all the commands.
     * It loads all the user commands and template commands, then it compares them
     * to inject the compatible templates that are not already present.
     * NOTE: it must be called after the application manager has been initialized.
     */
    fun initialize() {
        var userCommands = loadCommands()
        val templateCommands = templateLoader.getTemplateCommands()

        LOG.info("Loaded ${userCommands.size} user commands, joining with ${templateCommands.size} templates...")

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
                commandMap[command.id!!] = command as CommandWrapper
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
                commandMap[maxId] = template as CommandWrapper
                saveCommand(template)
            }
        }
    }



    /**
     * Load all the saved commands
     */
    private fun loadCommands() : List<Command> {
        val commands = mutableListOf<Command>()

        maxId = 0

        for (file in commandDir.listFiles()) {
            val command = readCommandFromFile(file)
            commands.add(command)

            if (command.id!! > maxId) {
                maxId = command.id!!
            }
        }

        return commands
    }

    /**
     * Add and save a new command in the system.
     * It searches for conflicts with the existing commands and,
     * if the given command is already present in the system,
     * return the old one. If the command is really new, a new id is
     * given and the command is saved.
     */
    @Synchronized
    fun addCommand(command: Command) : Command {
        val hash = command.contentHash()
        var conflicting = false

        if (conflictMap[hash] != null && conflictMap[hash]!!.any { it.contentEquals(command) }) {
            conflicting = true
        }

        if (conflicting) {
            // Find and retrieve the conflicting one
            val conflictingCommand = conflictMap[hash]!!.filter { it.contentEquals(command) }.first()
            return conflictingCommand
        }else{
            command as CommandWrapper

            // Give an id and specify the author to the current command
            command.id = ++maxId
            command.author = "user"
            commandMap[command.id!!] = command
            saveCommand(command)

            // Add the command to the conflict map
            if (conflictMap[hash] == null) {
                conflictMap[hash] = mutableListOf(command as Command)
            }else{
                conflictMap[hash]!!.add(command)
            }

            return command
        }
    }

    /**
     * Save the changes of an existing command.
     * To add a NEW command, use addCommand() instead.
     */
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

    fun deleteCommand(command: Command) : Boolean {
        command as CommandWrapper
        command.deleted = true
        return saveCommand(command)
    }

    /**
     * Read and parse the command from the specified JSON file
     */
    private fun readCommandFromFile(commandFile : File) : Command {
        val commandParser = commandParser
        val fis = FileInputStream(commandFile)
        val tokener = JSONTokener(fis)
        val jsonContent = JSONObject(tokener)
        fis.close()
        return commandParser.fromJSON(jsonContent)
    }

    override fun getCommand(id: Int): Command? {
        return commandMap[id]
    }

    /**
     * Return all the commands t
     */
    fun searchCommands(query : String? = null, limit: Int = 0, activeApplication: Application? = null,
                       showImplicit : Boolean = true, showDeleted : Boolean = false) : Collection<Command> {
        var results = commandMap.values.toList()

        if (!showImplicit) {
            results = results.filter { it.implicit == false }
        }

        if (!showDeleted) {
            results = results.filter { it.deleted == false }
        }

        if (query != null) {
            val filteringFunction : (Command) -> Boolean = if (query.startsWith(":")) {
                { it: Command ->
                    it.quickCommand?.startsWith(query, true) ?: false
                }
            }else{
                { it: Command ->
                    it.title?.contains(query, true) ?: false ||
                            it.description?.contains(query, true) ?: false
                }
            }


            if (activeApplication != null) {
                // Find all the commands related to the app and then the not related ones
                val appCommands = getAppRelatedCommands().filter { it.app == activeApplication.executablePath && filteringFunction(it) }
                val noAppCommands = commandMap.values.filter {
                    !(it is AppRelatedCommand)||
                            it.app != activeApplication.executablePath
                }.filter(filteringFunction)

                results = appCommands + noAppCommands
            }else{
                results = results.filter(filteringFunction)
            }
        }

        return if (limit > 0) {
            results.take(limit)
        } else{
            results
        }
    }

    fun getAppRelatedCommands() : Collection<AppRelatedCommand> {
        return commandMap.values.filter { it is AppRelatedCommand }.map { it as AppRelatedCommand }.filter { it.app != null }
    }

    fun getCommandsForApp(appPath: String) : Collection<AppRelatedCommand> {
        return getAppRelatedCommands().filter { it.app == appPath }
    }

    // Used in the section selector bar
    fun getAppOpenCommand(appPath: String) : Command? {
        return commandMap.values.find { it is AppOpenCommand && it.executablePath == appPath }
    }
}