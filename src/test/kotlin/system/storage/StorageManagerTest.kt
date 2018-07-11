package system.storage

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class StorageManagerTest {
    companion object {
        fun createMockStorageManager() :StorageManager {
            if (File("testDir").exists()) {
                FileUtils.deleteDirectory(File("testDir"))
            }
            val testDir = File("testDir")
            testDir.mkdir()

            return BaseDirectoryStorageManager(testDir)
        }

        fun cleanMockStorageManager() {
            FileUtils.deleteDirectory(File("testDir"))
        }
    }

    private var storageManager : StorageManager? = null

    @BeforeEach
    fun setUp() {
        storageManager = createMockStorageManager()
    }

    @AfterEach
    fun tearDown() {
        cleanMockStorageManager()
        storageManager = null
    }

    @Test
    fun testDirectoriesAreCreated() {
        assertTrue(storageManager!!.cacheDir.isDirectory)
        assertTrue(storageManager!!.storageDir.isDirectory)
        assertTrue(storageManager!!.iconCacheDir.isDirectory)
        assertTrue(storageManager!!.webCacheDir.isDirectory)
        assertTrue(storageManager!!.sectionDir.isDirectory)
        assertTrue(storageManager!!.commandDir.isDirectory)
    }

    @Test
    fun testClearCacheDirectory() {
        assertTrue(storageManager!!.cacheDir.isDirectory)

        File(storageManager!!.cacheDir, "testFile").createNewFile()

        assertTrue(File(storageManager!!.cacheDir, "testFile").isFile)

        storageManager!!.clearCache()

        assertFalse(File(storageManager!!.cacheDir, "testFile").isFile)
    }
}