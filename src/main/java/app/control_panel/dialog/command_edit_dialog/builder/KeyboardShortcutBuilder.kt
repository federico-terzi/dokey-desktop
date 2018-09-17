package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.IconButton
import app.ui.control.RoundBorderButton
import app.ui.control.StyledLabel
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.general.KeyboardShortcutCommand

@RegisterBuilder(type = KeyboardShortcutCommand::class)
class KeyboardShortcutBuilder(val context: BuilderContext) : CommandBuilder {
    override val contentBox = VBox()

    init {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER

        val button = RoundBorderButton("ALT")
        hBox.children.addAll(button)

        contentBox.children.add(hBox)
    }

    override fun populateUIForCommand(command: Command) {

    }

    override fun updateCommand(command: Command) {

    }

    override fun validateInput(): Boolean {

        return false
    }
}