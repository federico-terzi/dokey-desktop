package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand
class FolderOpenCommand : SimpleCommand() {
    init {
        category = "folderopen"
    }

    var folder : String?
        get() = this.value
        set(folder) {
            value = folder
        }
}