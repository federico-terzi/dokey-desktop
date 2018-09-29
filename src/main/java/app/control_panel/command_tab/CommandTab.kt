package app.control_panel.command_tab

import app.alert.AlertFactory
import app.control_panel.ControlPanelStage
import app.control_panel.ControlPanelTab
import app.control_panel.command_tab.list.CommandActionListener
import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.ui.panel.CommandListPanel
import app.ui.control.FloatingActionButton
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import model.command.Command
import system.BroadcastManager
import system.SettingsManager
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.commands.exporter.CommandExporter
import system.commands.importer.CommandImporter
import system.commands.model.CommandWrapper
import system.image.ImageResolver
import java.io.File
import java.util.*

class CommandTab(val controlPanelStage: ControlPanelStage, val imageResolver: ImageResolver,
                 val resourceBundle: ResourceBundle, val applicationManager: ApplicationManager,
                 val commandManager: CommandManager, val commandExporter: CommandExporter,
                 val commandImporter: CommandImporter, val settingsManager: SettingsManager) : ControlPanelTab(), CommandActionListener {

    // UI Elements
    private val toolbar = CommandToolbar(controlPanelStage, imageResolver)
    private val commandListPanel = CommandListPanel(controlPanelStage, imageResolver, commandManager,
            showImplicit = false, showContextMenus = true, commandActionListener = this)
    private val stackPane = StackPane()
    private val addCommandBtn = FloatingActionButton(imageResolver, "Add command")  // TODO: i18n

    init {
        commandListPanel.padding = Insets(0.0, 0.0, 38.0, 0.0)

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

        toolbar.onFilterUpdate = { filter ->
            commandListPanel.filter(filter)
        }

        commandListPanel.onCommandSelected = { command ->
            command as CommandWrapper
            if (!command.deleted) {
                requestEditForCommand(command)
            }else{
                showCommandIsDeletedRecoverDialog(command)
            }
        }
    }

    override fun onFocus() {
        commandListPanel.showDeleted = settingsManager.showDeletedCommands
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
            commandListPanel.loadCommandsAndFocus(commandId)
        }
    }

    private fun showCommandIsDeletedRecoverDialog(command: CommandWrapper) {
        AlertFactory.instance.confirmation("Deleted command", "This command was previously deleted, do you want to recover it?",  // TODO: i18n
                onYes = {
                    commandManager.undeleteCommand(command)
                    commandListPanel.loadCommands()
                }).show()
    }

    /**
     * CONTEXT MENU ACTIONS
     */

    override val onEditRequest: ((Command) -> Unit)? = { command ->
        requestEditForCommand(command)
    }

    override val onDeleteRequest: ((List<Command>) -> Unit)? = {commands ->
        AlertFactory.instance.confirmation("Delete confirmation", "Do you really want to delete ${commands.size} command(s)?",  // TODO: i18n
                onYes = {
                    commands.forEach { command ->
                        commandManager.deleteCommand(command)
                    }
                    commandListPanel.loadCommands()
                }).show()
    }
    override val onRecoverRequest: ((List<Command>) -> Unit)? = {commands ->
        commands.forEach { command ->
            commandManager.undeleteCommand(command)
        }
        commandListPanel.loadCommands()
    }
    override val onExportRequest: ((List<Command>) -> Unit)? = {commands ->
        val fileChooser = FileChooser()
        fileChooser.title = "Export commands..."  // TODO: i18n
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Commands", "*.dkcm"))

        val destinationFile = fileChooser.showSaveDialog(null)
        if (destinationFile != null) {
            commandExporter.export(commands, destinationFile)
        }
    }
    override val onImportRequest: (() -> Unit)? = {
        val fileChooser = FileChooser()
        fileChooser.title = "Import commands..."  // TODO: i18n
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Commands", "*.dkcm"))

        val sourceFile = fileChooser.showOpenDialog(null)
        if (sourceFile != null) {
            val commands = commandImporter.import(sourceFile)
            if (commands.isNotEmpty()) {
                commandListPanel.loadCommandsAndFocus(commands.first().id!!)
            }
        }
    }

    private fun requestEditForCommand(command: Command) {
        val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
        dialog.loadCommand(command)
        dialog.showWithAnimation()
    }
}