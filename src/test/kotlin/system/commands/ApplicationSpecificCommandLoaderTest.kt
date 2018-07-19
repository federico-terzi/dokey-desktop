package system.commands

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import system.app_manager.MockApplicationManager
import system.commands.general.SimpleAppRelatedCommand
import system.commands.loader.ApplicationSpecificCommandLoader
import system.model.ApplicationManager
import system.parsers.RuntimeModelParser
import system.storage.StorageManager
import system.storage.StorageManagerTest

class ApplicationSpecificCommandLoaderTest {
    var storageManager : StorageManager? = null
    var appManager : ApplicationManager? = null
    var applicationSpecificCommandLoader : ApplicationSpecificCommandLoader? = null

    @BeforeEach
    fun setUp() {
        storageManager = StorageManagerTest.createMockStorageManager()
        appManager = MockApplicationManager(storageManager!!)
        applicationSpecificCommandLoader = ApplicationSpecificCommandLoader(appManager!!, RuntimeModelParser(MockCommandResolver()),
                storageManager!!)
    }

    @AfterEach
    fun tearDown() {
        StorageManagerTest.cleanMockStorageManager()
        storageManager = null
        appManager = null
        applicationSpecificCommandLoader = null
    }

    @Test
    fun testLoadTemplates() {
        // Make sure the chrome template has been loaded
        assertTrue(applicationSpecificCommandLoader!!.templateMap["chrome.exe"] != null)
    }

    @Test
    fun testCompatibleCommands() {
        val commands = applicationSpecificCommandLoader!!.getCompatibleCommandTemplates()
        assertTrue(commands.filter {it as SimpleAppRelatedCommand
            it.title == "New Tab" && it.app == "C:\\Programs\\chrome.exe"}.isNotEmpty())
    }
}