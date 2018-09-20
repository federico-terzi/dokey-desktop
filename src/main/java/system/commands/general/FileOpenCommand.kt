package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "Open File", iconId = "asset:file")
class FileOpenCommand : SimpleCommandWrapper() {
    init {
        category = "fileopen"
        iconId = "static:insert_drive_file"
    }

    var file : String?
        get() = this.value
        set(file) {
            value = file
        }
}