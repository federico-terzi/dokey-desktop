package system.drag_and_drop

import javafx.scene.input.Dragboard
import model.command.Command
import system.commands.CommandManager
import system.commands.general.FileOpenCommand
import system.commands.general.FolderOpenCommand
import system.commands.general.UrlCommand
import system.web.WebResolver
import java.io.File

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
    fun resolve(dragboard: Dragboard, callback: ((Command?) -> Unit)) : Unit {
        if (dragboard.hasString() && dragboard.string.startsWith(dragAndDropPrefix)) {
            // Extract the type and the payload from the dragboard data
            // The data is in this form: DOKEY_PAYLOAD:type:payload
            val type = dragboard.string.split(":")[1]
            val payload = dragboard.string.split("$dragAndDropPrefix:$type:")[1]
            when (type) {
                "command" -> {
                    val commandId = payload.toInt()
                    callback(commandManager.getCommand(commandId))
                }
            }
        }else if(dragboard.hasFiles() && dragboard.files.size > 0) {
            val file = dragboard.files[0]
            if (file.isFile) {
                callback(createFileCommand(file))
            }else if(file.isDirectory) {
                callback(createFolderCommand(file))
            }
        }else if(dragboard.hasUrl() && dragboard.url.startsWith("http")) {
            val url = dragboard.url
            Thread {
                val urlCommand = createUrlCommand(url)
                callback(urlCommand)
            }.start()
        }
    }

    private fun createFileCommand(file: File) : Command {
        val absolutePath = file.absolutePath
        val name = file.name

        val command = FileOpenCommand()
        command.file = absolutePath
        command.title = name
        command.description = absolutePath

        return commandManager.addCommand(command)
    }

    private fun createFolderCommand(folder: File) : Command {
        val absolutePath = folder.absolutePath
        val name = folder.name

        val command = FolderOpenCommand()
        command.folder = absolutePath
        command.title = name
        command.description = absolutePath

        return commandManager.addCommand(command)
    }

    private fun createUrlCommand(url: String) : Command {
        val command = UrlCommand()
        command.url = url
        command.title = WebResolver.extractTitleFromUrl(url)
        command.description = url

        return commandManager.addCommand(command)
    }
}