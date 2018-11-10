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
import system.commands.general.SystemCommand
import system.system.SystemAction

@RegisterBuilder(type = SystemCommand::class)
class SystemBuilder(val context: BuilderContext, val parent: BlurrableStage) : CommandBuilder {
    override val contentBox = VBox()
    private val typeComboBox = StyledComboBox<SystemAction>()

    init {
        contentBox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        contentBox.alignment = Pos.CENTER
        contentBox.children.addAll(typeComboBox)

        typeComboBox.promptText = "Select Type..."  // TODO: i18n
        typeComboBox.items = FXCollections.observableArrayList(SystemAction.values().toList())
    }

    override fun populateUIForCommand(command: Command) {
        command as SystemCommand

        typeComboBox.selectionModel.select(command.actionType)

        // If command is locked then disable the button
        if (command.locked) {
            contentBox.isDisable = true
        }
    }

    override fun updateCommand(command: Command) {
        command as SystemCommand

        command.actionType = typeComboBox.value
    }

    override fun validateInput() {
        if (typeComboBox.value == null) {
            throw ValidationException("Please choose a system action.") // TODO: i18n
        }
    }
}