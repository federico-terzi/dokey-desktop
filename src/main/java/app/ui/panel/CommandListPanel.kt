package app.ui.panel

import app.control_panel.command_tab.CommandListHeader
import app.control_panel.command_tab.list.CommandActionListener
import app.control_panel.command_tab.list.CommandListView
import app.control_panel.command_tab.list.comparator.NameComparator
import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.ui.control.FloatingActionButton
import app.ui.model.Sorting
import app.ui.stage.BlurrableStage
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.Comparator

class CommandListPanel(val parent: BlurrableStage, val imageResolver: ImageResolver, val commandManager: CommandManager,
                       var showImplicit: Boolean = true,
                       val showContextMenus : Boolean = false, commandActionListener: CommandActionListener? = null)
    : StackPane() {

    // UI Elements
    private val listHeader = CommandListHeader(imageResolver)
    private val commandListView = CommandListView(imageResolver, commandActionListener, showContextMenus = showContextMenus)

    // This is the list that will contain the commands shown by the list view
    private val commands = FXCollections.observableArrayList<Command>()

    private var currentComparator: Comparator<Command> = NameComparator(Sorting.ASCENDING)


    private var currentQuery: String? = null
    private var currentFilter: Class<out Command>? = null

    var onCommandSelected : ((Command) -> Unit)? = null
    var onNewCommandRequested : (() -> Unit)? = null

    private val addCommandBtn = FloatingActionButton(imageResolver, "New command")  // TODO: i18n

    private val contentBox = VBox()


    init {
        alignment = Pos.BOTTOM_RIGHT

        VBox.setVgrow(commandListView, Priority.ALWAYS)

        // Move the addCommandButton a bit higher
        addCommandBtn.translateY = -40.0

        contentBox.children.addAll(listHeader, commandListView)
        children.addAll(contentBox, addCommandBtn)

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

        // Setup add command button listener
        addCommandBtn.setOnAction {
            onNewCommandRequested?.invoke()
        }
    }

    fun loadCommands(onLoaded: (() -> Unit)? = null) {
        Thread {
            var results = commandManager.searchCommands(query = currentQuery,
                    showImplicit = showImplicit).toMutableList()

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

    fun loadCommandsAndFocus(commandId: Int) {
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