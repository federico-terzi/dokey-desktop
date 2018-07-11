package system.startup

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import system.startup.MSStartupManager.STARTUP_LINK_FILENAME
import system.storage.StorageManager
import system.storage.StorageManagerTest
import java.io.File

class MSStartupManagerTest {
    var storageManager : StorageManager? = null

    @BeforeEach
    fun setUp() {
        storageManager = StorageManagerTest.createMockStorageManager()
    }

    @AfterEach
    fun tearDown() {
        StorageManagerTest.cleanMockStorageManager()
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun testCreateLink() {
        val startupManager = MSStartupManager(storageManager)

        val testFile = File("testFile")
        if (!testFile.isFile) {
            testFile.createNewFile()
        }

        val finalFile = File(storageManager!!.cacheDir, STARTUP_LINK_FILENAME)

        assertFalse(finalFile.isFile)

        startupManager.createLinkFile(testFile.absolutePath)

        val linkFile = File(storageManager!!.cacheDir, STARTUP_LINK_FILENAME)

        assertTrue(linkFile.isFile)

        testFile.delete()
    }



}