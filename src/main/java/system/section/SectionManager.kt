package system.section

import json.JSONException
import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.component.Component
import model.component.RuntimeComponent
import model.page.DefaultPage
import model.page.Page
import model.parser.section.SectionParser
import model.section.*
import org.apache.commons.codec.digest.DigestUtils
import system.BroadcastManager
import system.applications.Application
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.commands.general.AppRelatedCommand
import system.section.model.DefaultSectionWrapper
import system.section.model.SectionWrapper
import system.storage.StorageManager
import java.io.*
import java.util.logging.Logger


/**
 * The SectionManager is used to manage sections.
 */
class SectionManager(val storageManager: StorageManager, val sectionParser: SectionParser,
                     val commandManager: CommandManager, val applicationManager: ApplicationManager) {
    // A cache that associates the section id with the section
    private val sectionCache = mutableMapOf<String, SectionWrapper>()

    fun getSections(filterDeleted: Boolean = true) : Collection<SectionWrapper> {
        return sectionCache.values.filter {
            filterDeleted && !it.deleted
        }
    }

    fun getSection(sectionId: String, filterDeleted: Boolean = true) : SectionWrapper? {
        val section = sectionCache[sectionId]
        if (section != null) {
            if (!section.deleted || !filterDeleted) {
                return section
            }
        }

        return null
    }

    fun createSectionForApp(application: Application) : Section {
        val section = getSection("app:${application.id}", filterDeleted = false)

        if (section == null) {  // Section doesn't exist
            val applicationSection = generateEmptyAppSection(application.id, application.name)
            saveSection(applicationSection)
            sectionCache[applicationSection.id!!] = applicationSection

            return applicationSection
        }else{
            if (section.deleted) {
                // The section already exists, but it is hidden because has been deleted
                undeleteSection(section)
            }

            return section
        }
    }

    private fun loadSections(): Collection<SectionWrapper> {
        val sections = mutableListOf<SectionWrapper>()

        // Go through all user section files
        for (sectionFile in storageManager.sectionDir.listFiles()) {
            // Skip hidden files
            if (sectionFile.isHidden)
                continue

            // Get the section from the file
            val currentSection = getSectionFromFile(sectionFile)
            if (currentSection != null) {
                // Validate the section and remove invalid commands
                validateSection(currentSection)

                sections.add(currentSection)
            }
        }

        return sections
    }

    fun initialize() {
        // Get all the application commands, grouped by application
        val applications = commandManager.getAppRelatedCommands().groupBy { it.app }

        // Generate all the missing sections based on the available applications
        for (application in applications.keys) {
            // Get the application name and make sure the app was found
            val applicationName = applicationManager.getApplication(application) ?: continue

            // Check if the application already has an associated section.
            val sectionFile = getSectionFile("app:$application")
            if (!sectionFile.isFile) {  // If not, generate a new section from the commands
                val section = generateAppSectionFromCommands(application!!, applicationName.name, applications[application]!!)
                saveSection(section)
            }
        }

        // Generate the launchpad section if missing
        if (!getSectionFile("launchpad").isFile) {
            saveSection(generateLaunchpadSection())
        }

        // Generate the system section if missing
        if (!getSectionFile("system").isFile) {
            saveSection(generateSystemSection())
        }

        // Load all the sections and populate the section cache
        val sections = loadSections()
        sections.forEach { sectionCache[it.id!!] = it }

        // Register broadcast listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.EDITOR_MODIFIED_COMMAND_EVENT, editorCommandModifiedListener)
    }

    /**
     * Analyze the commands of the section to make sure that all of them exists.
     * If an unknown command is found, remove the command.
     */
    fun validateSection(section: Section) {
        val toBeDeleted = mutableListOf<Component>()

        section.pages?.forEach { page ->
            page.components?.forEach { component ->
                // If the command id is not found, remove the component
                if (commandManager.getCommand(component.commandId!!) == null) {
                    toBeDeleted.add(component)
                }
            }
        }

        if (toBeDeleted.size > 0) {
            LOG.warning("Removing invalid commands from section: ${section.name}: ${toBeDeleted.map { it.commandId }.joinToString(",")}")

            section.pages?.forEach {page ->
                page.components?.removeAll(toBeDeleted)
            }

            // Update the section
            saveSection(section)
        }
    }

    /**
     * Cycle through all sections, to remove the specified command
     */
    fun deleteCommandFromAllSections(command: Command) {
        sectionCache.values.forEach { section ->
            var hasBeenUpdated = false

            section.pages?.forEach { page ->
                val toBeDeleted = mutableListOf<Component>()

                page.components?.forEach { component ->
                    // Delete the specified command
                    if (component.commandId == command.id) {
                        toBeDeleted.add(component)
                    }
                }

                if (toBeDeleted.size > 0) {
                    page.components?.removeAll(toBeDeleted)
                    hasBeenUpdated = true
                }
            }

            if (hasBeenUpdated) {
                saveSection(section)
            }
        }
    }

    /**
     * Return a list of all the sections containing the specified command.
     */
    fun getAllSectionsContainingCommand(commandId: Int) : List<Section> {
        val output = mutableListOf<Section>()
        sectionLoop@ for (section in sectionCache.values) {
            if (section.pages != null) {
                for (page in section.pages!!) {
                    if (page.components != null) {
                        for (component in page.components!!) {
                            if (component.commandId == commandId) {
                                output.add(section)
                                continue@sectionLoop
                            }
                        }
                    }
                }
            }
        }
        return output
    }

    private fun generateEmptyPage(colCount: Int = DEFAULT_PAGE_COLS, rowCount: Int = DEFAULT_PAGE_ROWS) : Page {
        val page = DefaultPage()
        page.colCount = colCount
        page.rowCount = rowCount
        page.components = mutableListOf()
        return page
    }

    private fun generateSystemSection(): SectionWrapper {
        // Get the available system commands in the system
        val systemCommands = commandManager.getSystemCommands().toList()
        return generateSectionFromCommands(SystemSection::class.java, sectionName = "System", commands = systemCommands,
                pageRows = 3, pageCols = 3)
    }

    private fun generateLaunchpadSection(): SectionWrapper {
        val section = LaunchpadSection()
        section.name = "Launchpad"
        section.pages = mutableListOf(generateEmptyPage())
        return DefaultSectionWrapper(section)
    }

    private fun generateEmptyAppSection(appId: String, applicationName: String) : SectionWrapper {
        // Create the destination section
        val section = DefaultApplicationSection()
        section.id = "app:$appId"
        section.name = applicationName
        section.pages = mutableListOf(generateEmptyPage())
        return DefaultSectionWrapper(section)
    }

    private fun generateSectionFromCommands(sectionClass: Class<out Section>, sectionName: String, commands: List<Command>,
                                            sectionId: String? = null,
                                            pageCols : Int = DEFAULT_PAGE_COLS, pageRows: Int = DEFAULT_PAGE_ROWS): SectionWrapper {
        // Create the destination section
        val section = sectionClass.newInstance()

        section.name = sectionName
        if (sectionId != null) {
            section.id = sectionId
        }


        // Create a list of pages by filling them with commands
        val pages = mutableListOf<Page>(generateEmptyPage(colCount = pageCols, rowCount = pageRows))

        var currentPage = 0
        var currentX = 0
        var currentY = 0

        commands.forEach { command ->
            if (currentY >= pageRows ) {
                currentX = 0
                currentY = 0
                currentPage++

                val page = generateEmptyPage(colCount = pageCols, rowCount = pageRows)
                pages.add(page)
            }

            val component = RuntimeComponent(commandManager)
            component.commandId = command.id
            component.x = currentX
            component.y = currentY
            pages[currentPage].components?.add(component)

            currentX++
            if (currentX >= pageCols) {
                currentX = 0
                currentY++
            }
        }

        section.pages = pages
        return DefaultSectionWrapper(section)
    }

    private fun generateAppSectionFromCommands(appId: String, applicationName: String, commands: List<AppRelatedCommand>): SectionWrapper {
        return generateSectionFromCommands(DefaultApplicationSection::class.java, sectionName = applicationName,
                commands = commands, sectionId = "app:$appId")
    }

    private fun getSectionFile(sectionId: String) : File {
        // Get the Hash of the section id, used to create the filename
        val md5hash = DigestUtils.md5Hex(sectionId)

        return File(storageManager.sectionDir, "$md5hash.json")
    }

    private fun getSectionFile(section: Section) : File {
        return getSectionFile(section.id!!)
    }

    @Synchronized
    fun saveSection(section: Section): Boolean {
        val sectionFile: File = getSectionFile(section)

        // Update the last edit
        section.lastEdit = System.currentTimeMillis()

        // Reload the section in the cache
        sectionCache[section.id!!] = section as SectionWrapper

        // Save the section
        return writeSectionToFile(section, sectionFile)
    }

    /**
     * Write the given section to the given file
     *
     * @param section the Section to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    @Synchronized
    private fun writeSectionToFile(section: Section, dest: File?): Boolean {
        try {
            // Write the json section to the file
            val fos = FileOutputStream(dest)
            val pw = PrintWriter(fos)
            val sectionJson = section.json()  // Get the section json
            pw.write(sectionJson.toString())
            pw.close()

            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    @Synchronized
    fun deleteSection(section: Section): Boolean {
        section as SectionWrapper
        section.deleted = true

        return saveSection(section)
    }

    @Synchronized
    fun undeleteSection(section: Section): Boolean {
        section as SectionWrapper
        section.deleted = false

        return saveSection(section)
    }

    @Synchronized
    fun resetSection(section: Section): Section? {
        val newSection : Section? = when (section.type) {
            "launchpad" -> {
                generateLaunchpadSection()
            }
            "system" -> {
                generateSystemSection()
            }
            "app" -> {
                section as ApplicationSection
                val application = applicationManager.getApplication(section.appId)
                if (application != null) {
                    val commands = commandManager.getAppRelatedCommands().filter { it.app == section.appId }
                    generateAppSectionFromCommands(application.id, application.name, commands)
                }else{
                    generateEmptyAppSection(section.appId!!, section.name!!)
                }
            }
            else -> null
        }

        if (newSection != null) {
            newSection.name = section.name
            newSection.id = section.id

            if (saveSection(newSection)) {
                return newSection
            }
        }

        return null
    }


    private val editorCommandModifiedListener = BroadcastManager.BroadcastListener {
        val commandId = (it as String?)?.toInt()
        if (commandId != null) {
            val sections = getAllSectionsContainingCommand(commandId)
            sections.forEach {section ->
                saveSection(section)
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, section.id)
            }
        }
    }

    /**
     * Read a section from the given file
     * @param sectionFile the section File
     * @return the Section read from the file
     */
    fun getSectionFromFile(sectionFile: File): SectionWrapper? {
        // Read the content
        try {
            val fis = FileInputStream(sectionFile)
            val tokener = JSONTokener(fis)
            val jsonContent = JSONObject(tokener)

            // Create the section by de-serialization
            val section = sectionParser.fromJSON(jsonContent)
            val wrapper = DefaultSectionWrapper(section)
            wrapper.populateWrapperFields(jsonContent)

            fis.close()

            return wrapper
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        val DEFAULT_PAGE_ROWS = 4
        val DEFAULT_PAGE_COLS = 4

        val LOG = Logger.getGlobal()
    }
}