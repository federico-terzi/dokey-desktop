package system.section

import json.JSONException
import json.JSONObject
import json.JSONTokener
import model.component.RuntimeComponent
import model.page.DefaultPage
import model.page.Page
import model.parser.section.SectionParser
import model.section.DefaultApplicationSection
import model.section.LaunchpadSection
import model.section.Section
import model.section.SystemSection
import org.apache.commons.codec.digest.DigestUtils
import system.commands.CommandManager
import system.commands.general.AppRelatedCommand
import system.storage.StorageManager
import java.io.*


/**
 * The SectionManager is used to manage sections.
 */
class SectionManager(val storageManager: StorageManager, val sectionParser: SectionParser,
                     val commandManager: CommandManager) {
    // A cache that associates the section id with the section
    val sectionCache = mutableMapOf<String, Section>()

    init {
        // Load all the sections and populate the section cache
        val sections = loadSections()
        sections.forEach { sectionCache[it.id!!] = it }
    }

    fun getSections() : Collection<Section> {
        return sectionCache.values
    }

    private fun loadSections(): Collection<Section> {
        val sections = mutableListOf<Section>()

        // Go through all user section files
        for (sectionFile in storageManager.sectionDir.listFiles()) {
            // Skip hidden files
            if (sectionFile.isHidden)
                continue

            // Get the section from the file
            val currentSection = getSectionFromFile(sectionFile)
            if (currentSection != null) {
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
            // Check if the application already has an associated section.
            val sectionFile = getSectionFile("app:$application")
            if (!sectionFile.isFile) {  // If not, generate a new section from the commands
                val section = generateAppSectionFromCommands(application!!, applications[application]!!)
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
    }

    private fun generateEmptyPage() : Page {
        val page = DefaultPage()
        page.colCount = DEFAULT_PAGE_COLS
        page.rowCount = DEFAULT_PAGE_ROWS
        page.components = mutableListOf()
        return page
    }

    private fun generateSystemSection(): Section {
        val section = SystemSection()
        section.pages = mutableListOf(generateEmptyPage())
        return section
    }

    private fun generateLaunchpadSection(): LaunchpadSection {
        val section = LaunchpadSection()
        section.pages = mutableListOf(generateEmptyPage())
        return section
    }

    private fun generateAppSectionFromCommands(application: String, commands: List<AppRelatedCommand>): Section {
        // Create the destination section
        val section = DefaultApplicationSection()
        section.id = "app:$application"

        // Create a list of pages by filling them with commands
        val pages = mutableListOf<Page>(generateEmptyPage())

        var currentPage = 0
        var currentX = 0
        var currentY = 0

        commands.forEach { command ->
            if (currentY >= DEFAULT_PAGE_ROWS ) {
                currentX = 0
                currentY = 0
                currentPage++

                val page = generateEmptyPage()
                pages.add(page)
            }

            val component = RuntimeComponent(commandManager)
            component.commandId = command.id
            component.x = currentX
            component.y = currentY
            pages[currentPage].components?.add(component)

            currentX++
            if (currentX >= DEFAULT_PAGE_COLS) {
                currentX = 0
                currentY++
            }
        }

        section.pages = pages
        return section
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
        val sectionFile: File = getSectionFile(section)
        if (sectionFile.isFile) {
            return sectionFile.delete()
        }

        return false
    }

    /**
     * Read a section from the given file
     * @param sectionFile the section File
     * @return the Section read from the file
     */
    fun getSectionFromFile(sectionFile: File): Section? {
        // Read the content
        try {
            val fis = FileInputStream(sectionFile)
            val tokener = JSONTokener(fis)
            val jsonContent = JSONObject(tokener)

            // Create the section by de-serialization
            val section = sectionParser.fromJSON(jsonContent)

            fis.close()

            return section
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
    }
}