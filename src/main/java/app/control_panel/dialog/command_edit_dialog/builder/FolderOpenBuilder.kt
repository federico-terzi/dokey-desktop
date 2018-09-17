package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.stage.BlurrableStage
import javafx.scene.input.TransferMode
import javafx.stage.DirectoryChooser
import model.command.Command
import system.commands.general.FolderOpenCommand
import java.io.File

@RegisterBuilder(type = FolderOpenCommand::class)
class FolderOpenBuilder(context: BuilderContext, val parent: BlurrableStage) : ImageTextCommandBuilder(context, "asset:folder") {
    init {
        label.text = "Drop folder here or click to select..."  // TODO: i18n

        label.setOnMouseClicked {
            promptChooseDirectory()
        }
        imageView.setOnAction {
            promptChooseDirectory()
        }

        // Setup drag and drop
        label.setOnDragOver{ event ->
            if (event.dragboard.hasFiles() && event.dragboard.files[0].isDirectory) {
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
            }

            event.consume()
        }
        label.setOnDragDropped { event ->
            var success = false

            if (event.dragboard.hasFiles() && event.dragboard.files[0].isDirectory) {
                label.text = event.dragboard.files[0].absolutePath
                success = true
            }

            event.isDropCompleted = success

            event.consume()
        }
    }

    private fun promptChooseDirectory() {
        val directoryChooser = DirectoryChooser()
        val selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null && selectedDirectory.isDirectory) {
            label.text = selectedDirectory.absolutePath
        }
    }

    override fun populateUIForCommand(command: Command) {
        command as FolderOpenCommand

        label.text = command.folder
    }

    override fun updateCommand(command: Command) {
        command as FolderOpenCommand

        command.folder = label.text
    }

    override fun validateInput() {
        if (label.text.isBlank()) {
            throw ValidationException("Please select a directory.") // TODO: i18n
        }

        // Make sure the folder exists
        val folder = File(label.text)
        if (!folder.isDirectory) {
            throw ValidationException("The selected directory does not exist.") // TODO: i18n
        }
    }


}