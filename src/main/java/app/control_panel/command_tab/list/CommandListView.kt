package app.control_panel.command_tab.list

import app.ui.control.StyledMenuItem
import javafx.collections.ListChangeListener
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import model.command.Command
import system.image.ImageResolver

class CommandListView(val imageResolver: ImageResolver) : ListView<Command>() {
    var onEditRequest : ((Command) -> Unit)? = null
    var onDeleteRequest : ((List<Command>) -> Unit)? = null

    // Context menu items
    private val editItem : MenuItem = StyledMenuItem("/assets/edit.png", "Edit")  // TODO: i18n
    private val deleteItem : MenuItem = StyledMenuItem("/assets/delete.png", "Delete")  // TODO: i18n

    // Menu items that support only a single selection
    private val singleElementMenuItems : List<MenuItem> = listOf(editItem)

    init {
        styleClass.add("command-list-view")

        setCellFactory {
            CommandListCell(imageResolver)
        }

        selectionModel.selectionMode = SelectionMode.MULTIPLE

        // Setup the context menu
        val cm = ContextMenu()
        editItem.setOnAction {
            //onEditRequest?.invoke()
        }
        deleteItem.setOnAction {
            //onDeleteRequest?.invoke()
        }

        cm.items.addAll(editItem, deleteItem)
        contextMenu = cm

        // Add binding to show/hide context menu items based on the number of selected items
        this.selectionModel.selectedIndices.addListener(ListChangeListener<Int> {
            if (it.list.size >= 2) {
                singleElementMenuItems.forEach { it.isVisible = false }
            }else{
                singleElementMenuItems.forEach { it.isVisible = true }
            }
        })
    }
}