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
//
//    /**
//     * Read a section from the given file with the optional import parameters.
//     * @param sectionFile the section File
//     * @return the SectionWrapper read from the file
//     */
//    fun importSectionFromFile(sectionFile: File): SectionWrapper? {
//        // Read the content
//        try {
//            val fis = FileInputStream(sectionFile)
//            val tokener = JSONTokener(fis)
//            val jsonContent = JSONObject(tokener)
//
//            // Create the section by de-serialization
//            val importedSection = SectionWrapper.fromJson(jsonContent)
//
//            fis.close()
//
//            return importedSection
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return null
//    }
//
//    /**
//     * Get the list of items contained in the given section.
//     * @param section the section to analyze.
//     * @return the List of Items contained in the given section.
//     */
//    fun getSectionItems(section: Section): List<Item> {
//        val output = ArrayList<E>(30)
//
//        for (page in section.getPages()) {
//            for (component in page.components!!) {
//                output.add(component.getItem())
//            }
//        }
//
//        output.addAll(section.getBottomBarItems())
//
//        return output
//    }
//
//    /**
//     * Return the File of the Section associated with the given appPath.
//     * If a section is not available, it generates an empty one and then
//     * writes it to the file.
//     *
//     * @param appPath the application path.
//     * @return the File of the Section associated with the application.
//     */
//    private fun getAppSectionFile(appPath: String): File {
//        // Get the hash of the app path
//        val appPathHash = DigestUtils.md5Hex(appPath)
//
//        // Get the section directory
//        val userSectionDir = CacheManager.getInstance().getSectionDir()
//
//        // Get the section file
//        val sectionFile = File(userSectionDir, appPathHash + ".json")
//
//        // If the file doesn't exist, check if a template is available
//        // if not, fill it with an empty section.
//        if (!sectionFile.isFile()) {
//            // Check if a template is available
//            var section = getTemplateSectionForApp(appPath)
//
//            // If no template has been found, generate an empty section
//            if (section == null) {
//                section = generateEmptyShortcutSection(appPath)
//            }
//
//            // Write the section to the file
//            writeSectionToFile(section, sectionFile)
//        }
//
//        return sectionFile
//    }
//
//    /**
//     * Generate an empty Shortcut section for the given app.
//     *
//     * @param appPath the application path.
//     * @return a Shortcut Section associated to the given appPath.
//     */
//    private fun generateEmptyShortcutSection(appPath: String): Section {
//        // Create an empty section
//        val section = Section()
//        section.setSectionType(SectionType.SHORTCUTS)
//        section.setLastEdit(System.currentTimeMillis())
//        section.setRelatedAppId(appPath)
//
//        // Add an empty page
//        val firstPage = Page()
//        firstPage.setTitle("Page 1")
//        firstPage.colCount = DEFAULT_PAGE_COLS
//        firstPage.rowCount = DEFAULT_PAGE_ROWS
//        section.addPage(firstPage)
//
//        return section
//    }
//
//    /**
//     * Generate an empty section for the launchpad
//     *
//     * @return a Launchpad Section.
//     */
//    private fun generateEmptyLaunchpadSection(): Section {
//        // Create an empty section
//        val section = Section()
//        section.setSectionType(SectionType.LAUNCHPAD)
//        section.setLastEdit(System.currentTimeMillis())
//
//        val firstPage = Page()
//        firstPage.setTitle("Launchpad")
//        firstPage.colCount = 4
//        firstPage.rowCount = 4
//
//        // Add the default elements
//        val docsItem = WebLinkItem()
//        docsItem.setUrl("https://dokey.io/docs/")
//        docsItem.setTitle("Docs")
//        docsItem.setIconID("dokey")
//        val docsComponent = Component()
//        docsComponent.setItem(docsItem)
//        docsComponent.setX(0)
//        docsComponent.setY(0)
//
//        firstPage.addComponent(docsComponent)
//
//        section.addPage(firstPage)
//        return section
//    }
//
//    /**
//     * Search for a template for the given app path.
//     * @param appPath the path of the application. C:\...\prog.exe or /Applications/App.app
//     * @return the requested Section if a template was found, null otherwise.
//     */
//    private fun getTemplateSectionForApp(appPath: String): Section? {
//        // Extract the filename from the appPath
//        val appFile = File(appPath)
//        val appName = appFile.getName()
//
//        // Search in the templateMap
//        val templateFilename = templateMap[appName] ?: return null
//
//        // If a template was not found, return null
//
//        // Template was found, read the Section
//        val templateFile = ResourceUtils.getResource("/sections/$templateFilename")
//
//        // Read the section
//        val section = getSectionFromFile(templateFile)
//
//        // Replace the relatedAppID with the correct path and the correct modified id
//        if (section != null) {
//            section!!.setRelatedAppId(appPath)
//            section!!.setLastEdit(System.currentTimeMillis())
//        }
//
//        return section
//    }
//

//
//    /**
//     * Write the given sectionWrapper to the given file.
//     *
//     * @param sectionWrapper the SectionWrapper to save.
//     * @param dest    the destination File.
//     * @return true if succeeded, false otherwise.
//     */
//    @Synchronized
//    fun exportSectionToFile(sectionWrapper: SectionWrapper, dest: File): Boolean {
//        try {
//            // Write the json sectionWrapper to the file
//            val fos = FileOutputStream(dest)
//            val pw = PrintWriter(fos)
//            val sectionJson = sectionWrapper.json()
//            pw.write(sectionJson.toString())
//            pw.close()
//
//            return true
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return false
//    }
//
//    /**
//     * Load the template association into the map
//     */
//    private fun loadTemplateMap() {
//        // Get the template file
//        val templateDb = ResourceUtils.getResource("/sections/$TEMPLATE_DB_FILENAME")
//
//        // Reset the template map
//        templateMap = HashMap()
//
//        // Read all the file and populate the map
//        try {
//            val br = BufferedReader(InputStreamReader(FileInputStream(templateDb)))
//
//            var line: String
//            while ((line = br.readLine()) != null) {
//                line = line.trim { it <= ' ' }
//                val st = StringTokenizer(line, "=")
//                val appName = st.nextToken()
//                val templateFilename = st.nextToken()
//
//                // Add it to the map
//                templateMap[appName] = templateFilename
//            }
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }
//
    companion object {
        val DEFAULT_PAGE_ROWS = 4
        val DEFAULT_PAGE_COLS = 4
    }
}