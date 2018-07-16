package system.commands

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import system.storage.StorageManager
import system.storage.StorageManagerTest

class CommandManagerTest {
    var storageManager : StorageManager? = null

    @BeforeAll
    fun setupClass() {
        storageManager = StorageManagerTest.createMockStorageManager()
    }

    @AfterAll
    fun tearDownClass() {
        StorageManagerTest.cleanMockStorageManager()
        storageManager = null
    }

    @BeforeEach
    fun setUp() {

    }

    @AfterEach
    fun tearDown() {

    }




}