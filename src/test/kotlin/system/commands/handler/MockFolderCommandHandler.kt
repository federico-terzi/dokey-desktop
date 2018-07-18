package system.commands.handler

import org.junit.jupiter.api.Assertions.assertFalse
import system.DokeyContext
import system.commands.MockFolderCommand
import system.commands.annotations.RegisterHandler

@RegisterHandler(commandType = MockFolderCommand::class)
class MockFolderCommandHandler(context : DokeyContext) : CommandHandler<MockFolderCommand>(context) {
    override fun handleInternal(command: MockFolderCommand) {
        throw ReceivedException()
    }

    class ReceivedException : RuntimeException() {

    }
}