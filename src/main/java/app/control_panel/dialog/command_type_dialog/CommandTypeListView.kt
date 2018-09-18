package app.control_panel.dialog.command_type_dialog

import app.ui.control.SelectionListView
import app.ui.list_cell.ImageTextEntry
import app.ui.list_cell.ImageTextListCell
import javafx.collections.FXCollections
import model.command.Command
import org.reflections.Reflections
import system.commands.CommandDescriptor
import system.commands.annotations.RegisterCommand
import system.image.ImageResolver

class CommandTypeListView(val imageResolver: ImageResolver) : SelectionListView<CommandDescriptor>() {
    private val types = FXCollections.observableArrayList<CommandDescriptor>()

    init {
        setCellFactory {
            ImageTextListCell<CommandDescriptor>(imageResolver) {
                ImageTextEntry(it.iconId, it.title)
            }
        }

        items = types
        types.setAll(loadCommandDescriptors())
    }

    fun selectTypeForCommand(command: Command) {
        val index = types.indexOfFirst {
            it.associatedCommandClass == command::class.java
        }
        if (index >= 0) {
            selectionModel.select(index)
        }
    }

    fun filter(query: String?) {
        val filtered = if (query != null) {
            commandDescriptors.filter { it.title.contains(query, ignoreCase = true) }
        }else{
            commandDescriptors
        }
        types.setAll(filtered)
    }

    companion object {
        val commandDescriptors = loadCommandDescriptors()

        fun loadCommandDescriptors() : List<CommandDescriptor> {
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