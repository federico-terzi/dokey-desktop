package app.control_panel.command_tab

import app.control_panel.ControlPanelStage
import app.control_panel.ControlPanelTab
import app.control_panel.command_tab.list.CommandListView
import app.control_panel.command_tab.list.comparator.NameComparator
import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.ui.control.FloatingActionButton
import app.ui.model.Sorting
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import model.command.Command
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.*

class CommandTab(val controlPanelStage: ControlPanelStage, val imageResolver: ImageResolver,
                 val resourceBundle: ResourceBundle, val applicationManager: ApplicationManager,
                 val commandManager: CommandManager) : ControlPanelTab() {

    // UI Elements
    private val toolbar = CommandToolbar(imageResolver)
    private val listHeader = CommandListHeader(imageResolver)
    private val stackPane = StackPane()
    private val commandListView = CommandListView(imageResolver)
    private val addCommandBtn = FloatingActionButton(imageResolver, "Add command")  // TODO: i18n

    // This is the list that will contain the commands shown by the list view
    private val commands = FXCollections.observableArrayList<Command>()

    private var currentQuery: String? = null
    private var currentComparator: Comparator<Command> = NameComparator(Sorting.ASCENDING)

    init {
        VBox.setVgrow(commandListView, Priority.ALWAYS)

        stackPane.alignment = Pos.BOTTOM_RIGHT

        // Move the addCommandButton a bit higher
        addCommandBtn.translateY = -40.0

        stackPane.children.addAll(commandListView, addCommandBtn)
        VBox.setVgrow(stackPane, Priority.ALWAYS)

        children.addAll(toolbar, listHeader, stackPane)

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

        // Setup add command button listener
        addCommandBtn.setOnAction {
            val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
            dialog.showWithAnimation()
        }
    }

    fun loadCommands() {
        val results = commandManager.searchCommands(query = currentQuery)
        commands.setAll(results)
        commands.sortWith(currentComparator)
    }

    override fun onFocus() {
        loadCommands()
    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }
}