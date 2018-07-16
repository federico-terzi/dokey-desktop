package system.commands

import model.parser.DesktopModelParser
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import system.app_manager.MockApplicationManager
import system.commands.general.SimpleAppRelatedCommand
import system.commands.general.KeyboardShortcutCommand
import system.model.ApplicationManager
import system.storage.StorageManager
import system.storage.StorageManagerTest

class AppCommandLoaderTest {
    var storageManager : StorageManager? = null
    var appManager : ApplicationManager? = null
    var appCommandLoader : AppCommandLoader? = null

    @BeforeEach
    fun setUp() {
        storageManager = StorageManagerTest.createMockStorageManager()
        appManager = MockApplicationManager(storageManager!!)
        appCommandLoader = AppCommandLoader(appManager!!, DesktopModelParser(MockCommandResolver(), listOf(MockFolderCommand::class.java,
                KeyboardShortcutCommand::class.java)),
                storageManager!!)
    }

    @AfterEach
    fun tearDown() {
        StorageManagerTest.cleanMockStorageManager()
        storageManager = null
        appManager = null
        appCommandLoader = null
    }

    @Test
    fun testLoadTemplates() {
        // Make sure the chrome template has been loaded
        assertTrue(appCommandLoader!!.templateMap["chrome.exe"] != null)
    }

    @Test
    fun testCompatibleCommands() {
        val commands = appCommandLoader!!.getCompatibleCommandTemplates()
        assertTrue(commands.filter {it as SimpleAppRelatedCommand
            it.title == "New Tab" && it.app == "C:\\Programs\\chrome.exe"}.isNotEmpty())
    }
}