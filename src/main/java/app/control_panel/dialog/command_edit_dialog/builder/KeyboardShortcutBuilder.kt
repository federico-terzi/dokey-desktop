package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.app_select_dialog.ApplicationSelectDialog
import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.ApplicationSelectButton
import app.ui.control.RoundBorderButton
import app.ui.control.ShortcutField
import app.ui.stage.BlurrableStage
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.general.KeyboardShortcutCommand
import utils.OSValidator

@RegisterBuilder(type = KeyboardShortcutCommand::class)
class KeyboardShortcutBuilder(val context: BuilderContext, val parent: BlurrableStage) : CommandBuilder {
    private val specialKeys = listOf<String>("CTRL", "ALT", "ESCAPE", "ENTER", "DELETE", "SHIFT", "TAB")

    override val contentBox = VBox()
    private val applicationButton = ApplicationSelectButton(parent, context.imageResolver, context.applicationManager)
    private val shortcutField = ShortcutField()

    init {
        contentBox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        contentBox.spacing = 12.0
        contentBox.alignment = Pos.CENTER

        // Load the special keys
        val specialKeysBox = FlowPane()
        specialKeysBox.alignment = Pos.CENTER
        specialKeysBox.vgap = 5.0
        specialKeysBox.hgap = 5.0

        platformSpecialKeys().forEach { key ->
            val button = RoundBorderButton(key)
            specialKeysBox.children.add(button)

            button.setOnAction {
                shortcutField.addSpecialKey(key)
            }
        }

        contentBox.children.addAll(applicationButton, shortcutField, specialKeysBox)
    }

    override fun populateUIForCommand(command: Command) {
        command as KeyboardShortcutCommand

        shortcutField.loadShortcut(command.shortcut!!)

        val application = if (command.app != null ) {
            context.applicationManager.getApplication(command.app)
        }else{
            null
        }

        applicationButton.application = application

        // If command is locked then disable the button
        if (command.locked) {
            contentBox.isDisable = true
        }
    }

    override fun updateCommand(command: Command) {
        command as KeyboardShortcutCommand

        command.app = applicationButton.application?.id
        command.shortcut = shortcutField.getShortcut()
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