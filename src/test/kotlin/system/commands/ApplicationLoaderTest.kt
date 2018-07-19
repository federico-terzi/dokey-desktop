package system.commands

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import system.app_manager.MockApplicationManager
import system.commands.general.SimpleAppRelatedCommand
import system.commands.loader.ApplicationLoader
import system.model.ApplicationManager
import system.parsers.RuntimeModelParser
import system.storage.StorageManager
import system.storage.StorageManagerTest

class ApplicationLoaderTest {
    var storageManager : StorageManager? = null
    var appManager : ApplicationManager? = null
    var applicationLoader : ApplicationLoader? = null

    @BeforeEach
    fun setUp() {
        storageManager = StorageManagerTest.createMockStorageManager()
        appManager = MockApplicationManager(storageManager!!)
        applicationLoader = ApplicationLoader(appManager!!, RuntimeModelParser(MockCommandResolver()),
                storageManager!!)
    }

    @AfterEach
    fun tearDown() {
        StorageManagerTest.cleanMockStorageManager()
        storageManager = null
        appManager = null
        applicationLoader = null
    }

    @Test
    fun testLoadTemplates() {
        // Make sure the chrome template has been loaded
        assertTrue(applicationLoader!!.templateMap["chrome.exe"] != null)
    }

    @Test
    fun testCompatibleCommands() {
        val commands = applicationLoader!!.getCompatibleCommandTemplates()
        assertTrue(commands.filter {it as SimpleAppRelatedCommand
            it.title == "New Tab" && it.app == "C:\\Programs\\chrome.exe"}.isNotEmpty())
    }
}