package app.control_panel.command_tab

import app.control_panel.ControlPanelTab
import app.control_panel.command_tab.list.CommandListView
import app.control_panel.command_tab.list.comparator.NameComparator
import app.ui.model.Sorting
import javafx.application.Platform
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
    private val listHeader = CommandListHeader(imageResolver)
    private val commandListView = CommandListView(imageResolver)

    // This is the list that will contain the commands shown by the list view
    private val commands = FXCollections.observableArrayList<Command>()

    private var currentQuery: String? = null
    private var currentComparator: Comparator<Command> = NameComparator(Sorting.ASCENDING)

    init {
        VBox.setVgrow(commandListView, Priority.ALWAYS)

        children.addAll(toolbar, listHeader, commandListView)

        // Setup the list view
        commandListView.items = commands

        // Setup ordering logic
        listHeader.onSortingSelected = { comparator ->
            currentComparator = comparator
            loadCommands()
        }

        // Setup the search bar listener
        toolbar.onSearchChanged = { query ->
            if (query.isBlank()) {
                currentQuery = null
            }else{
                currentQuery = query
            }
            loadCommands()
        }
    }

    fun loadCommands() {
        Thread {
            val results = commandManager.searchCommands(query = currentQuery).toMutableList()
            results.sortWith(currentComparator)

            Platform.runLater {
                commands.setAll(results)
            }
        }.start()

    }

    override fun onFocus() {
        loadCommands()
    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }
}