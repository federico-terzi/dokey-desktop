package app.control_panel.dialog.command_select_dialog

import app.ui.panel.CommandListPanel
import app.ui.control.ExpandableSearchBar
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.CommandManager
import system.image.ImageResolver

class CommandSelectDialog(parent: BlurrableStage, imageResolver: ImageResolver, commandManager: CommandManager)
    : OverlayDialog(parent, imageResolver) {

    private val searchBar = ExpandableSearchBar(imageResolver)
    private val commandListPanel = CommandListPanel(parent, imageResolver, commandManager)
    private val contentBox = VBox()

    var onCommandSelected : ((Command) -> Unit)? = null

    init {
        commandListPanel.padding = Insets(0.0, 0.0,10.0, 0.0)
        commandListPanel.minHeight = 300.0
        commandListPanel.prefHeight = 1.0
        VBox.setVgrow(commandListPanel, Priority.ALWAYS)

        //contentBox.padding = Insets(0.0, 5.0, 10.0, 5.0)

        contentBox.alignment = Pos.CENTER
        contentBox.children.addAll(commandListPanel)

        initializeUI()

        searchBar.onSearchChanged = { query ->
            commandListPanel.search(query)
        }

        commandListPanel.onCommandSelected = {
            onCommandSelected?.invoke(it)
            onClose()
        }

        commandListPanel.loadCommands()
    }

    override fun defineTopSectionComponent(): Node? {
        return searchBar
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}