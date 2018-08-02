package system.drag_and_drop

import javafx.scene.input.Dragboard
import model.command.Command
import system.commands.CommandManager

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

    fun resolve(dragboard: Dragboard) : Command? {
        if (dragboard.hasString() && dragboard.string.startsWith(dragAndDropPrefix)) {
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
            if (file.isFile) {
                TODO()
            }else if(file.isDirectory) {
                TODO()
            }
        }else if(dragboard.hasUrl() && dragboard.url.startsWith("http")) {
            TODO()
        }

        return null
    }
}