package app.ui.panel

import app.control_panel.command_tab.CommandListHeader
import app.control_panel.command_tab.list.CommandListView
import app.control_panel.command_tab.list.comparator.NameComparator
import app.ui.model.Sorting
import app.ui.stage.BlurrableStage
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.Comparator

class CommandListPanel(val parent: BlurrableStage, val imageResolver: ImageResolver, val commandManager: CommandManager,
                       val showImplicit: Boolean = true, val showDeleted: Boolean = false)
    : VBox() {

    // UI Elements
    private val listHeader = CommandListHeader(imageResolver)
    private val commandListView = CommandListView(imageResolver)

    // This is the list that will contain the commands shown by the list view
    private val commands = FXCollections.observableArrayList<Command>()

    private var currentComparator: Comparator<Command> = NameComparator(Sorting.ASCENDING)


    private var currentQuery: String? = null
    private var currentFilter: Class<out Command>? = null

    var onCommandSelected : ((Command) -> Unit)? = null

    init {
        VBox.setVgrow(commandListView, Priority.ALWAYS)

        children.addAll(listHeader, commandListView)

        // Setup the list view
        commandListView.items = commands

        // Setup ordering logic
        listHeader.onSortingSelected = { comparator ->
            currentComparator = comparator
            loadCommands()
        }

        commandListView.setOnMouseClicked {
            if (it.clickCount == 2) {
                val command = commandListView.selectionModel.selectedItem
                if (command != null) {
                    onCommandSelected?.invoke(command)
                }
            }
        }
    }

    fun loadCommands(onLoaded: (() -> Unit)? = null) {
        Thread {
            var results = commandManager.searchCommands(query = currentQuery,
                    showImplicit = showImplicit, showDeleted = showDeleted).toMutableList()

            if (currentFilter != null) {
                results = results.filter { it.javaClass == currentFilter }.toMutableList()
            }

            results.sortWith(currentComparator)

            Platform.runLater {
                commands.setAll(results)

                onLoaded?.invoke()
            }
        }.start()
    }

    fun focusCommand(commandId: Int) {
        loadCommands {
            val selectedIndex = commands.indexOfFirst { it.id == commandId }
            if (selectedIndex >= 0) {
                commandListView.selectionModel.select(selectedIndex)
                commandListView.scrollTo(selectedIndex)
            }
        }
    }

    fun search(query: String?) {
        if (query != null && query.isEmpty()) {
            currentQuery = null
        }else{
            currentQuery = query
        }
        loadCommands()
    }

    fun filter(filter: Class<out Command>?) {
        currentFilter = filter
        loadCommands()
    }
}