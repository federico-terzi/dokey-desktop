package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "Open Folder", iconId = "asset:folder")
class FolderOpenCommand : SimpleCommandWrapper() {
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