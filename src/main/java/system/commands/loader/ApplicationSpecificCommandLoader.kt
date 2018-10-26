package system.commands.loader

import json.JSONArray
import json.JSONObject
import json.JSONTokener
import model.command.Command
import system.ResourceUtils
import system.commands.annotations.RegisterLoader
import system.commands.general.AppRelatedCommand
import system.context.CommandTemplateContext
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
            if (templateMap.containsKey(application.globalId)) {
                val appCommands = loadCommandsFromTemplateFile(templateMap[application.globalId]!!.file,
                        application.id)
                commands.addAll(appCommands)
            }
        }

        return commands
    }

    private fun loadCommandsFromTemplateFile(file : String, applicationId: String) : List<Command> {
        // Get the template file
        val templateFile = ResourceUtils.getResource("/commands/${CommandLoader.getOSPathSuffix()}/$file")

        val commandParser = context.commandParser
        val fis = FileInputStream(templateFile)
        val tokener = JSONTokener(fis)
        val jsonArray = JSONArray(tokener)

        val commands = mutableListOf<Command>()

        for (jsonObj in jsonArray) {
            val command = commandParser.fromJSON(jsonObj as JSONObject)

            // Set the specific application path
            command as AppRelatedCommand
            command.app = applicationId
            command.locked = true

            // Set the icon identifier if null
            if (command.iconId == null) {
                command.iconId = "app:$applicationId"
            }

            commands.add(command)
        }

        return commands
    }

    /**
     * Load the command templates configuration and populate the template map
     */
    private fun loadTemplates() {
        // Get the template file
        val templateDb = ResourceUtils.getResource("/commands/${CommandLoader.getOSPathSuffix()}/apps.json")

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