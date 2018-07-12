package system.commands

import model.command.Command
import system.model.ApplicationManager
import java.util.StringTokenizer
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import json.JSONArray
import json.JSONObject
import json.JSONTokener
import model.parser.ModelParser
import system.ResourceUtils
import java.io.*

class CommandTemplateLoader(val appManager: ApplicationManager, val modelParser: ModelParser) {
    val templateMap = mutableMapOf<String, TemplateEntry>()

    data class TemplateEntry(val appName : String, val file: String)

    init {
        loadTemplates()
    }

    /**
     * Return the list of command templates compatible with the current system.
     */
    fun getCompatibleCommandTemplates() : List<Command> {
        val commands = mutableListOf<Command>()

        for (application in appManager.applicationList) {
            val executableFile = File(application.executablePath)
            val executableName = executableFile.name

            if (templateMap.containsKey(executableName)) {
                val appCommands = loadCommandsFromTemplateFile(templateMap[executableName]!!.file)
                commands.addAll(appCommands)
            }
        }

        return commands
    }

    private fun loadCommandsFromTemplateFile(file : String) : List<Command> {
        // Get the template file
        val templateFile = ResourceUtils.getResource("/commands/$file")

        val commandParser = modelParser.commandParser
        val fis = FileInputStream(templateFile)
        val tokener = JSONTokener(fis)
        val jsonArray = JSONArray(tokener)

        val commands = mutableListOf<Command>()

        for (jsonObj in jsonArray) {
            val command = commandParser.fromJSON(jsonObj as JSONObject)
            commands.add(command)
        }

        return commands
    }

    /**
     * Load the command templates configuration and populate the template map
     */
    private fun loadTemplates() {
        // Get the template file
        val templateDb = ResourceUtils.getResource("/commands/templates.json")

        // Read all the file and populate the map
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(templateDb)))

            reader.useLines { lines -> lines.forEach {line ->
                val json = JSONObject(line)

                val appName = json.getString("appName")
                val file = json.getString("file")

                val templateEntry = TemplateEntry(appName, file)
                templateMap[appName] = templateEntry
            }}
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}