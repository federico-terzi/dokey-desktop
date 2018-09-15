package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand(title = "Open Folder", iconId = "asset:launch")
class FolderOpenCommand : SimpleCommand() {
    init {
        category = "folderopen"
        iconId = "static:folder_open"
    }

    var folder : String?
        get() = this.value
        set(folder) {
            value = folder
        }
}