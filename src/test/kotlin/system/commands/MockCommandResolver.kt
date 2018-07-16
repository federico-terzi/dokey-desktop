package system.commands

import model.command.Command
import model.component.CommandResolver

class MockCommandResolver : CommandResolver {
    override fun getCommand(id: Int): Command? {
        return when (id) {
            1 -> MockFolderCommand.getMockCommand(1)
            else -> null
        }
    }
}