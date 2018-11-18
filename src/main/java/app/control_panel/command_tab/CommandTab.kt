package app.control_panel.command_tab

import app.alert.AlertFactory
import app.control_panel.ControlPanelStage
import app.control_panel.ControlPanelTab
import app.control_panel.DropDialog
import app.control_panel.command_tab.list.CommandActionListener
import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.ui.panel.CommandListPanel
import app.ui.control.FloatingActionButton
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.DragEvent
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
import system.drag_and_drop.DNDCommandProcessor
import system.exceptions.IncompatibleOsException
import system.image.ImageResolver
import java.io.File
import java.util.*

class CommandTab(val controlPanelStage: ControlPanelStage, val imageResolver: ImageResolver,
                 val resourceBundle: ResourceBundle, val applicationManager: ApplicationManager,
                 val commandManager: CommandManager, val commandExporter: CommandExporter,
                 val commandImporter: CommandImporter, val settingsManager: SettingsManager,
                 val dndCommandProcessor: DNDCommandProcessor) : ControlPanelTab(), CommandActionListener {

    // UI Elements
    private val toolbar = CommandToolbar(controlPanelStage, imageResolver)
    private val commandListPanel = CommandListPanel(controlPanelStage, imageResolver, commandManager,
            showImplicit = false, showContextMenus = true, commandActionListener = this)


    // Reference to the dialog that opens when dragging something inside
    private var dropDialog : DropDialog? = null

    init {
        commandListPanel.padding = Insets(0.0, 0.0, 38.0, 0.0)

        VBox.setVgrow(commandListPanel, Priority.ALWAYS)

        children.addAll(toolbar, commandListPanel)

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
            } else {
                showCommandIsDeletedRecoverDialog(command)
            }
        }

        commandListPanel.onNewCommandRequested = {
            val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
            dialog.showWithAnimation()
        }

        // Setup the drag and drop importing
        setOnDragEntered {
            if (dropDialog == null) {
                // Check if the dropping file is a dokey exported layout
                if (validateDropPayload(it)) {
                    dropDialog = DropDialog(controlPanelStage, imageResolver)
                    dropDialog?.verifyPayload = this::validateDropPayload
                    dropDialog?.showWithAnimation()
                    dropDialog?.onDialogClosed = {
                        dropDialog = null
                    }
                    dropDialog?.onContentDropped = {dragboard ->
                        // Importing a DKCF command file
                        if (dragboard.hasFiles() && dragboard.files[0].isFile && dragboard.files[0].extension == "dkcf") {
                            importCommands(dragboard.files[0])
                        }else if (dndCommandProcessor.isCompatible(dragboard)) {  // Importing a general item
                            // Create a new command based on the dropped item
                            dndCommandProcessor.resolve(dragboard) { command ->
                                if (command != null) {
                                    Platform.runLater {
                                        commandListPanel.loadCommandsAndFocus(command.id!!)
                                    }
                                }
                            }
                        }
                    }
                }
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

    override val onDeleteRequest: ((List<Command>) -> Unit)? = { commands ->
        AlertFactory.instance.confirmation("Delete confirmation", "Do you really want to delete ${commands.size} command(s)?",  // TODO: i18n
                onYes = {
                    commands.forEach { command ->
                        commandManager.deleteCommand(command)
                    }
                    commandListPanel.loadCommands()
                }).show()
    }
    override val onRecoverRequest: ((List<Command>) -> Unit)? = { commands ->
        commands.forEach { command ->
            commandManager.undeleteCommand(command)
        }
        commandListPanel.loadCommands()
    }
    override val onExportRequest: ((List<Command>) -> Unit)? = { commands ->
        if (commands.isNotEmpty()) {
            val fileChooser = FileChooser()
            fileChooser.title = "Export commands..."  // TODO: i18n
            fileChooser.initialDirectory = File(System.getProperty("user.home"))
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Command Format", "*.dkcf"))

            // Generate an initial filename
            if (commands.size > 1) {
                fileChooser.initialFileName = "commands.dkcf"
            }else{
                fileChooser.initialFileName = "${commands.first().title}.dkcf"
            }

            val destinationFile = fileChooser.showSaveDialog(null)
            if (destinationFile != null) {
                commandExporter.export(commands, destinationFile)
            }
        }
    }
    override val onImportRequest: (() -> Unit)? = {
        val fileChooser = FileChooser()
        fileChooser.title = "Import commands..."  // TODO: i18n
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Command Format", "*.dkcf"))

        val sourceFile = fileChooser.showOpenDialog(null)
        if (sourceFile != null) {
            importCommands(sourceFile)
        }
    }

    private fun requestEditForCommand(command: Command) {
        val dialog = CommandEditDialog(controlPanelStage, imageResolver, applicationManager, commandManager)
        dialog.loadCommand(command)
        dialog.showWithAnimation()
    }

    fun importCommands(sourceFile: File) {
        try {
            val commandResult = commandImporter.import(sourceFile)
            // Show an alert if some commands could not be imported
            if (commandResult.failed.isNotEmpty()) {
                AlertFactory.instance.alert("Warning",  // TODO: i18n
                        "Some commands cannot not be imported because they are incompatible with your system: \n\n" +
                                "${commandResult.failed.map { "- " + it.title }.joinToString(separator = "\n")}"
                ).show()
            }

            if (commandResult.commands.isNotEmpty()) {
                commandListPanel.loadCommandsAndFocus(commandResult.commands.first().id!!)
            }
        } catch (ex: IncompatibleOsException) {
            AlertFactory.instance.alert("Incompatible command(s)",  // TODO: i18n
                    "Cannot import the requested commands because they are not compatible with your system."
            ).show()
        }
    }

    private fun validateDropPayload(event: DragEvent) : Boolean {
        return (event.dragboard.hasFiles() && event.dragboard.files[0].isFile && event.dragboard.files[0].extension == "dkcf")
                || dndCommandProcessor.isCompatible(event.dragboard)
    }
}