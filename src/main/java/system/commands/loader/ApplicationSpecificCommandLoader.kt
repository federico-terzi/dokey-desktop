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
class ApplicationSpecificCommandLoader(val context: CommandTemplateContext) : CommandLoader {
    val templateMap = mutableMapOf<String, TemplateEntry>()

    data class TemplateEntry(val appName : String, val file: String)

    init {
        loadTemplates()
    }

    override fun getTemplateCommands(): List<Command> {
        val commands = mutableListOf<Command>()

        for (application in context.applicationManager.applicationList) {
            val executableFile = File(application.executablePath)
            val executableName = executableFile.name

            if (templateMap.containsKey(executableName)) {
                val appCommands = loadCommandsFromTemplateFile(templateMap[executableName]!!.file,
                        application.executablePath)
                commands.addAll(appCommands)
            }
        }

        return commands
    }

    private fun loadCommandsFromTemplateFile(file : String, executablePath: String) : List<Command> {
        // Get the template file
        val templateFile = ResourceUtils.getResource("/commands/$file")

        val commandParser = context.commandParser
        val fis = FileInputStream(templateFile)
        val tokener = JSONTokener(fis)
        val jsonArray = JSONArray(tokener)

        val commands = mutableListOf<Command>()

        for (jsonObj in jsonArray) {
            val command = commandParser.fromJSON(jsonObj as JSONObject)

            // Set the specific application path
            command as AppRelatedCommand
            command.app = executablePath

            // Set the icon identifier
            command.iconId = "app:$executablePath"

            commands.add(command)
        }

        return commands
    }

    /**
     * Load the command templates configuration and populate the template map
     */
    private fun loadTemplates() {
        // Get the template file
        val templateDb = ResourceUtils.getResource("/commands/apps.json")

        // Read all the file and populate the map
        try {
            val fis = FileInputStream(templateDb)
            val tokener = JSONTokener(fis)
            val jsonArray = JSONArray(tokener)

            val commands = mutableListOf<Command>()

            for (json in jsonArray) {
                json as JSONObject

                val appName = json.getString("appName")
                val file = json.getString("file")

                val templateEntry = TemplateEntry(appName, file)
                templateMap[appName] = templateEntry
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}