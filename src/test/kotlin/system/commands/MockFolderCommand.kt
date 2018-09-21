package system.commands

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

//@RegisterCommand
class MockFolderCommand : SimpleCommand() {
    companion object {
        fun getMockCommand(id : Int) : MockFolderCommand {
            val command = MockFolderCommand()
            command.id = id
            command.title = "Open Documents"
            command.description = "This will open documents folder"
            command.iconId="static:folder"
            command.lastEdit = 5678
            command.folder = "C:\\test"
            return command
        }
    }

    override var category: String?
        get() = "folder"
        set(value) {}

    var folder : String?
        get() = value
        set(folder) {value = folder}
}