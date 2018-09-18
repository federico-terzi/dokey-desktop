package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand(title = "Open File", iconId = "asset:file")
class FileOpenCommand : SimpleCommand() {
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