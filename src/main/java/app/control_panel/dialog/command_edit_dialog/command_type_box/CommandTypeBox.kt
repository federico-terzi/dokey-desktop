package app.control_panel.dialog.command_edit_dialog.command_type_box

import app.ui.control.StyledComboBox
import app.ui.list_cell.ImageTextEntry
import app.ui.list_cell.ImageTextListCell
import javafx.collections.FXCollections
import model.command.Command
import org.reflections.Reflections
import system.commands.CommandDescriptor
import system.commands.annotations.RegisterCommand
import system.image.ImageResolver

class CommandTypeBox(val imageResolver: ImageResolver) : StyledComboBox<CommandDescriptor>() {
    init {
        setCellFactory {
            ImageTextListCell<CommandDescriptor>(imageResolver) {
                ImageTextEntry(it.iconId, it.title)
            }
        }

        promptText = "Select type" // TODO: i18n

        items = FXCollections.observableArrayList(getCommandDescriptors())
    }

    companion object {
        fun getCommandDescriptors() : List<CommandDescriptor> {
            val descriptorList = mutableListOf<CommandDescriptor>()

            // Load all the command handlers dynamically
            val reflections = Reflections("system.commands")
            val commands = reflections.getTypesAnnotatedWith(RegisterCommand::class.java)
            commands.forEach { commandClass ->
                val annotation = commandClass.getAnnotation(RegisterCommand::class.java)
                val descriptor = CommandDescriptor(annotation.title, annotation.iconId, commandClass as Class<out Command>)
                descriptorList.add(descriptor)
            }

            descriptorList.sortBy { it.title }

            return descriptorList
        }
    }
}