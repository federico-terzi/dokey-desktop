package app.control_panel.command_tab

import app.control_panel.ControlPanelTab
import app.control_panel.command_tab.list.CommandListView
import javafx.collections.FXCollections
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.*

class CommandTab(val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                 val commandManager: CommandManager) : ControlPanelTab() {

    // UI Elements
    private val toolbar = CommandToolbar(imageResolver)
    private val listHeader = CommandListHeader()
    private val commandListView = CommandListView(imageResolver)

    // This is the list that will contain the commands shown by the list view
    private val commands = FXCollections.observableArrayList<Command>()

    init {
        VBox.setVgrow(commandListView, Priority.ALWAYS)

        children.addAll(toolbar, listHeader, commandListView)

        // Setup the list view
        commandListView.items = commands
    }

    override fun onFocus() {
        val results = commandManager.searchCommands()
        commands.setAll(results)
    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }
}