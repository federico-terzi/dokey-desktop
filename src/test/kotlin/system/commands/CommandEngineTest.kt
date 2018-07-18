package system.commands

import org.junit.jupiter.api.*
import system.DokeyContext
import system.MockDokeyContext
import system.commands.handler.MockFolderCommandHandler
import system.model.ApplicationManager
import system.storage.StorageManager
import system.storage.StorageManagerTest

class CommandEngineTest {
    var commandEngine : CommandEngine? = null

    @BeforeEach
    fun setUp() {
        commandEngine = CommandEngine(MockDokeyContext())
    }

    @AfterEach
    fun tearDown() {
        commandEngine = null
    }

    @Test
    fun testCommandHandlerCalled() {
        val command = MockFolderCommand.getMockCommand(1)
        assertThrows<MockFolderCommandHandler.ReceivedException> {
            commandEngine?.execute(command)
        }
    }
}