package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.IconButton
import app.ui.control.StyledTextField
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.general.FolderOpenCommand
import java.io.File

@RegisterBuilder(type = FolderOpenCommand::class)
class FolderOpenBuilder(val context: BuilderContext) : CommandBuilder {
    override val contentBox = VBox()
    private val folderImage = IconButton(context.imageResolver, "asset:folder", 24)
    private val pathField = StyledTextField()

    init {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        HBox.setHgrow(pathField, Priority.ALWAYS)

        pathField.isDisable = true
        pathField.promptText = "Drop folder here or click to select..."  // TODO: i18n

        hBox.children.addAll(folderImage, pathField)

        contentBox.children.add(hBox)
    }

    override fun populateUIForCommand(command: Command) {
        command as FolderOpenCommand

        pathField.text = command.folder
    }

    override fun updateCommand(command: Command) {
        command as FolderOpenCommand

        command.folder = pathField.text
    }

    override fun validateInput(): Boolean {
        if (pathField.text.isBlank()) {
            throw ValidationException("Please select a directory.") // TODO: i18n
        }

        // Make sure the folder exists
        val folder = File(pathField.text)
        if (!folder.isDirectory) {
            throw ValidationException("The selected directory does not exist.") // TODO: i18n
        }

        return true
    }


}