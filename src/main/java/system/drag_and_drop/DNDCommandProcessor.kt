package system.drag_and_drop

import javafx.scene.input.Dragboard
import model.command.Command
import system.commands.CommandManager
import system.commands.general.FileOpenCommand
import system.commands.general.FolderOpenCommand

class DNDCommandProcessor(val commandManager: CommandManager) {
    companion object {
        val dragAndDropPrefix = "DOKEY_PAYLOAD"
    }

    /**
     * Check if the given dragboard is compatible with the importing
     */
    fun isCompatible(dragboard: Dragboard) : Boolean {
        if (dragboard.hasString() && dragboard.string.startsWith(dragAndDropPrefix)) {
            return true
        }else if(dragboard.hasFiles() && dragboard.files.size > 0) {
            return true
        }else if(dragboard.hasUrl() && dragboard.url.startsWith("http")) {
            return true
        }

        return false
    }

    /**
     * Obtain ( or create ) a command from the given dragboard data
     */
    fun resolve(dragboard: Dragboard) : Command? {
        if (dragboard.hasString() && dragboard.string.startsWith(dragAndDropPrefix)) {
            // Extract the type and the payload from the dragboard data
            // The data is in this form: DOKEY_PAYLOAD:type:payload
            val type = dragboard.string.split(":")[1]
            val payload = dragboard.string.split("$dragAndDropPrefix:$type:")[1]
            when (type) {
                "command" -> {
                    val commandId = payload.toInt()
                    return commandManager.getCommand(commandId)
                }
            }
        }else if(dragboard.hasFiles() && dragboard.files.size > 0) {
            val file = dragboard.files[0]
            val absolutePath = file.absolutePath
            val name = file.name
            if (file.isFile) {
                val command = FileOpenCommand()
                command.file = absolutePath
                command.title = name
                command.description = absolutePath
                val finalCommand = commandManager.addCommand(command)
                return finalCommand
            }else if(file.isDirectory) {
                val command = FolderOpenCommand()
                command.folder = absolutePath
                command.title = name
                command.description = absolutePath
                val finalCommand = commandManager.addCommand(command)
                return finalCommand
            }
        }else if(dragboard.hasUrl() && dragboard.url.startsWith("http")) {
            TODO()
        }

        return null
    }
}