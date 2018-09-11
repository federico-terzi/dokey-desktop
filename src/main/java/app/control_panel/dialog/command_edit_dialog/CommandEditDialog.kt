package app.control_panel.dialog.command_edit_dialog

import app.control_panel.ControlPanelStage
import app.ui.control.CollapseExpandButton
import app.ui.control.ExpandableSearchBar
import app.ui.dialog.OverlayDialog
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.applications.Application
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver

class CommandEditDialog(controlPanelStage: ControlPanelStage, imageResolver: ImageResolver,
                        val applicationManager: ApplicationManager, val commandManager: CommandManager)
    : OverlayDialog(controlPanelStage, imageResolver) {

    private val saveButton = Button("Save")
    private val contentBox = VBox()

    init {
        contentBox.alignment = Pos.CENTER

//        contentBox.children.addAll(applicationListView, showAllAppsBtn)

        initializeUI()
    }

    override fun defineTopSectionComponent(): Node? {
        return saveButton
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}