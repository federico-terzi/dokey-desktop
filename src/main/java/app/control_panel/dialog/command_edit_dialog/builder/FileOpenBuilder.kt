package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.stage.BlurrableStage
import javafx.scene.input.TransferMode
import javafx.stage.FileChooser
import model.command.Command
import system.commands.general.FileOpenCommand
import java.io.File

@RegisterBuilder(type = FileOpenCommand::class)
class FileOpenBuilder(context: BuilderContext, val parent: BlurrableStage) : ImageTextCommandBuilder(context, "asset:file") {
    init {
        label.text = "Drop file here or click to select..."  // TODO: i18n

        label.setOnMouseClicked {
            promptChooseFile()
        }
        imageView.setOnAction {
            promptChooseFile()
        }

        // Setup drag and drop
        label.setOnDragOver{ event ->
            if (event.dragboard.hasFiles() && event.dragboard.files[0].isFile) {
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
            }

            event.consume()
        }
        label.setOnDragDropped { event ->
            var success = false

            if (event.dragboard.hasFiles() && event.dragboard.files[0].isFile) {
                label.text = event.dragboard.files[0].absolutePath
                success = true
            }

            event.isDropCompleted = success

            event.consume()
        }
    }

    private fun promptChooseFile() {
        val fileChooser = FileChooser()
        val selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null && selectedFile.isFile) {
            label.text = selectedFile.absolutePath
        }
    }

    override fun populateUIForCommand(command: Command) {
        command as FileOpenCommand

        label.text = command.file
    }

    override fun updateCommand(command: Command) {
        command as FileOpenCommand

        command.file = label.text
    }

    override fun validateInput() {
        if (label.text.isBlank()) {
            throw ValidationException("Please select a file.") // TODO: i18n
        }

        // Make sure the file exists
        val file = File(label.text)
        if (!file.isFile) {
            throw ValidationException("The selected file does not exist.") // TODO: i18n
        }
    }


}