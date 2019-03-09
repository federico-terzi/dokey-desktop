package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.StyledComboBox
import app.ui.stage.BlurrableStage
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.specific.PhotoshopCommand
import system.external.photoshop.PhotoshopCommandAction

@RegisterBuilder(type = PhotoshopCommand::class)
class PhotoshopCommandBuilder(val context: BuilderContext, val parent: BlurrableStage) : CommandBuilder {
    override val contentBox = VBox()
    private val typeComboBox = StyledComboBox<PhotoshopCommandAction>()

    init {
        contentBox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        contentBox.spacing = 7.0
        contentBox.alignment = Pos.CENTER

        contentBox.children.addAll(typeComboBox)

        typeComboBox.promptText = "Select Type..."  // TODO: i18n
        typeComboBox.items = FXCollections.observableArrayList(PhotoshopCommandAction.values().toList())
    }

    override fun populateUIForCommand(command: Command) {
        command as PhotoshopCommand

        typeComboBox.selectionModel.select(command.command)

        // If command is locked then disable the button
        if (command.locked) {
            contentBox.isDisable = true
        }
    }

    override fun updateCommand(command: Command) {
        command as PhotoshopCommand

        command.command = typeComboBox.value
    }

    override fun validateInput() {
        if (typeComboBox.value == null) {
            throw ValidationException("Please choose a Photoshop slider type") // TODO: i18n
        }
    }
}