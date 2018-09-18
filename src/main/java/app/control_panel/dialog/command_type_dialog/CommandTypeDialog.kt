package app.control_panel.dialog.command_type_dialog

import app.ui.control.ExpandableSearchBar
import app.ui.control.StyledListView
import app.ui.control.grid_view.GridView
import app.ui.control.grid_view.model.GridViewEntry
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.commands.CommandDescriptor
import system.image.ImageResolver

class CommandTypeDialog(parent: BlurrableStage, imageResolver: ImageResolver)
    : OverlayDialog(parent, imageResolver) {

    private val searchBar = ExpandableSearchBar(imageResolver)
    private val commandListView = CommandTypeListView(imageResolver)
    private val contentBox = VBox()

    var onTypeSelected : ((CommandDescriptor?) -> Unit)? = null

    init {
        width = 280.0

        commandListView.minHeight = 200.0
        commandListView.prefHeight = 1.0
        VBox.setVgrow(commandListView, Priority.ALWAYS)

        contentBox.padding = Insets(0.0, 5.0, 10.0, 5.0)

        contentBox.alignment = Pos.CENTER
        contentBox.children.addAll(commandListView)

        initializeUI()

        searchBar.onSearchChanged = {
            commandListView.filter(it)
        }

        commandListView.setOnMouseClicked {
            val descriptor = commandListView.selectionModel.selectedItem
            if (descriptor != null) {
                onTypeSelected?.invoke(descriptor)
                onClose()
            }
        }
    }

    override fun defineTopSectionComponent(): Node? {
        return searchBar
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}