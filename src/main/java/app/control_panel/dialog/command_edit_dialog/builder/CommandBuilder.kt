package app.control_panel.dialog.command_edit_dialog.builder

import javafx.scene.layout.VBox
import model.command.Command

interface CommandBuilder {
    val contentBox : VBox

    fun populateUIForCommand(command: Command)

    fun updateCommand(command: Command)
}