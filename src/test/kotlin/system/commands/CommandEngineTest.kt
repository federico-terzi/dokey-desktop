/*
package system.commands

import org.junit.jupiter.api.*
import system.MockGeneralContext
import system.commands.handler.MockFolderCommandHandler

class CommandEngineTest {
    var commandEngine : CommandEngine? = null

    @BeforeEach
    fun setUp() {
        commandEngine = CommandEngine(MockGeneralContext())
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
}*/
