package app.control_panel.command_tab

import app.control_panel.ControlPanelStage
import app.control_panel.ControlPanelTab
import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.ui.panel.CommandListPanel
import app.ui.control.FloatingActionButton
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import system.BroadcastManager
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.*

class CommandTab(val controlPanelStage: ControlPanelStage, val imageResolver: ImageResolver,
                 val resourceBundle: ResourceBundle, val applicationManager: ApplicationManager,
                 val commandManager: CommandManager) : ControlPanelTab() {

    // UI Elements
    private val toolbar = CommandToolbar(controlPanelStage, imageResolver)
    private val commandListPanel = CommandListPanel(controlPanelStage, imageResolver, commandManager,
            showImplicit = false)
    private val stackPane = StackPane()
    private val addCommandBtn = FloatingActionButton(imageResolver, "Add command")  // TODO: i18n

    init {
        commandListPanel.padding = Insets(0.0, 0.0,38.0, 0.0)

        VBox.setVgrow(commandListPanel, Priority.ALWAYS)

        stackPane.alignment = Pos.BOTTOM_RIGHT

        // Move the addCommandButton a bit higher
        addCommandBtn.translateY = -40.0

        stackPane.children.addAll(commandListPanel, addCommandBtn)
        VBox.setVgrow(stackPane, Priority.ALWAYS)

        children.addAll(toolbar, stackPane)

        // Setup add command button listener
        addCommandBtn.setOnAction {
            val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
            dialog.showWithAnimation()
        }

        // Setup the search bar listener
        toolbar.onSearchChanged = { query ->
            commandListPanel.search(query)
        }

        toolbar.onFilterUpdate = {filter ->
            commandListPanel.filter(filter)
        }

        commandListPanel.onCommandSelected = { command ->
            val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
            dialog.loadCommand(command)
            dialog.showWithAnimation()
        }
    }

    override fun onFocus() {
        commandListPanel.loadCommands()

        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.EDITOR_MODIFIED_COMMAND_EVENT, commandModifiedEvent)
    }

    override fun onUnfocus() {
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.EDITOR_MODIFIED_COMMAND_EVENT, commandModifiedEvent)
    }

    private val commandModifiedEvent = BroadcastManager.BroadcastListener { commandIdString ->
        commandIdString as String
        val commandId = commandIdString.toInt()

        Platform.runLater {
            commandListPanel.focusCommand(commandId)
        }
    }
}