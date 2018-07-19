package system.commands.handler

import system.context.GeneralContext
import system.commands.MockFolderCommand
import system.commands.annotations.RegisterHandler

@RegisterHandler(commandType = MockFolderCommand::class)
class MockFolderCommandHandler(context : GeneralContext) : CommandHandler<MockFolderCommand>(context) {
    override fun handleInternal(command: MockFolderCommand) {
        throw ReceivedException()
    }

    class ReceivedException : RuntimeException() {

    }
}