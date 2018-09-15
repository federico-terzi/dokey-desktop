package app.control_panel.dialog.command_edit_dialog.command_type_box

import app.ui.control.StyledComboBox
import app.ui.list_cell.ImageTextEntry
import app.ui.list_cell.ImageTextListCell
import system.commands.type.CommandType
import system.image.ImageResolver

class CommandTypeBox(val imageResolver: ImageResolver) : StyledComboBox<CommandType>() {
    init {
        setCellFactory {
            ImageTextListCell<CommandType>(imageResolver) {
                ImageTextEntry(it.iconId, it.title)
            }
        }

        promptText = "Select type" // TODO: i18n

        // TODO : loading of command types using reflection.
    }
}