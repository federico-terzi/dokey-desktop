package system.section.importer

import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.Component
import model.parser.section.SectionParser
import model.section.ApplicationSection
import model.section.Section
import system.ApplicationPathResolver
import system.commands.importer.CommandImporter
import system.exceptions.IncompatibleOsException
import system.section.SectionManager
import system.section.model.DefaultSectionWrapper
import utils.OSValidator
import java.io.File

class SectionImporter(val sectionManager: SectionManager, val sectionParser: SectionParser,
                      val commandImporter: CommandImporter, val applicationPathResolver: ApplicationPathResolver) {

    data class Result(val section: Section?, val failedCommands: List<Command>)

    fun checkIfSectionAlreadyExists(sourceFile: File): Boolean {
        sourceFile.inputStream().use { stream ->
            val tokener = JSONTokener(stream)
            val json = JSONObject(tokener)

            val sectionId = json.getJSONObject("section").getString("id")
            return sectionManager.getSection(sectionId) != null
        }
    }

    fun import(sourceFile: File): Result {
        sourceFile.inputStream().use { stream ->
            val tokener = JSONTokener(stream)
            val json = JSONObject(tokener)

            return import(json)
        }
    }

    fun import(json: JSONObject): Result {
        // Check if the OS is compatible
        val os = json.getString("os")
        if (os != "any" && os != OSValidator.TAG) {
            throw IncompatibleOsException()
        }

        // Parse the section
        val section = sectionParser.fromJSON(json.getJSONObject("section"))
        // Make sure that if a section is related to an app, the app exists in the current system
        if (section is ApplicationSection) {
            // Find the path of the application in the current system
            val relatedApp = applicationPathResolver.searchApp(section.appId)
            if (relatedApp == null) {
                throw RelatedApplicationNotFoundException()
            }
            // Update the section id to reflect the new app path
            section.id = "app:$relatedApp"
        }


        // Load command metadata
        val commandJson = json.getJSONObject("commands")

        // Now add all the commands to the system
        val commandResult = commandImporter.import(commandJson)
        val newCommandMap = mutableMapOf<Int, Command>()
        commandResult.commands.forEach { newCommandMap[it.id!!] = it }

        // This list will hold all the commands that cannot be imported
        val toBeDeleted = mutableListOf<Component>()

        // Correct all the section command references
        section.pages?.forEach { page ->
            for (component in page.components ?: emptyList<Component>()) {
                // Get the id of the new one
                val newId = commandResult.newIdMapping[component.commandId]
                // Get the new command associated with the new id
                val newCommand = newCommandMap[newId]

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
        val sectionWrapper = DefaultSectionWrapper(section)
        if (sectionManager.saveSection(sectionWrapper)) {
            return Result(sectionWrapper, failedCommands = commandResult.failed)
        }

        return Result(null, failedCommands = commandResult.failed)
    }
}