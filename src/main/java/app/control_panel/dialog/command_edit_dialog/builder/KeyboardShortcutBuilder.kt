package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.RoundBorderButton
import app.ui.control.ShortcutField
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.general.KeyboardShortcutCommand
import utils.OSValidator

@RegisterBuilder(type = KeyboardShortcutCommand::class)
class KeyboardShortcutBuilder(val context: BuilderContext) : CommandBuilder {
    private val specialKeys = listOf<String>("CTRL", "ALT", "ESCAPE", "ENTER", "DELETE", "SHIFT", "TAB")

    override val contentBox = VBox()
    private val shortcutField = ShortcutField()

    init {
        contentBox.padding = Insets(10.0, 0.0, 0.0, 0.0)
        contentBox.spacing = 12.0

        // TODO: add target application

        val specialKeysBox = FlowPane()
        specialKeysBox.alignment = Pos.CENTER
        specialKeysBox.vgap = 5.0
        specialKeysBox.hgap = 5.0

        platformSpecialKeys().forEach { key ->
            val button = RoundBorderButton(key)
            specialKeysBox.children.add(button)

            button.setOnAction {
                shortcutField.addKey(key)
            }
        }

        contentBox.children.addAll(shortcutField, specialKeysBox)
    }

    override fun populateUIForCommand(command: Command) {
        command as KeyboardShortcutCommand

        // TODO: add application target

        shortcutField.loadShortcut(command.shortcut!!)
    }

    override fun updateCommand(command: Command) {
        command as KeyboardShortcutCommand

        command.shortcut = shortcutField.getShortcut()

        // TODO: add application target
    }

    override fun validateInput() {
        if (shortcutField.getShortcut() == null) {
            throw ValidationException("Please insert the Keyboard Shortcut.") // TODO: i18n
        }
    }

    private fun platformSpecialKeys() : List<String> {
        val platformKey : String? = if (OSValidator.isWindows()) {
            "WIN"
        }else if (OSValidator.isMac()) {
            "CMD"
        }else{
            null
        }

        if (platformKey != null) {
            val output = mutableListOf(platformKey)
            output.addAll(specialKeys)
            return output
        }else{
            return specialKeys
        }
    }
}