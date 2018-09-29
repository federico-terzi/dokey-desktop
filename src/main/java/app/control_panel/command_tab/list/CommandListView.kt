package app.control_panel.command_tab.list

import app.ui.control.StyledMenuItem
import javafx.collections.ListChangeListener
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import model.command.Command
import system.image.ImageResolver

class CommandListView(val imageResolver: ImageResolver, val commandActionListener: CommandActionListener?, showContextMenus: Boolean) : ListView<Command>(){
    // Context menu items
    private val editItem : MenuItem = StyledMenuItem("/assets/edit.png", "Edit")  // TODO: i18n
    private val deleteItem : MenuItem = StyledMenuItem("/assets/delete.png", "Delete")  // TODO: i18n
    private val exportItem : MenuItem = StyledMenuItem("/assets/external-link.png", "Export")  // TODO: i18n
    private val importItem : MenuItem = StyledMenuItem("/assets/import.png", "Import")  // TODO: i18n

    // Menu items that support only a single selection
    private val singleElementMenuItems : List<MenuItem> = listOf(editItem)

    init {
        styleClass.add("command-list-view")

        setCellFactory {
            CommandListCell(imageResolver)
        }

        selectionModel.selectionMode = SelectionMode.MULTIPLE

        // Setup the context menu
        if (showContextMenus) {
            val cm = ContextMenu()
            editItem.setOnAction {
                val selectedCommand = this.selectionModel.selectedItem
                if (selectedCommand != null) {
                    commandActionListener?.onEditRequest?.invoke(selectedCommand)
                }
            }
            exportItem.setOnAction {
                val selectedCommands = this.selectionModel.selectedItems
                if (selectedCommands.size > 0) {
                    commandActionListener?.onExportRequest?.invoke(selectedCommands)
                }
            }
            importItem.setOnAction {
                commandActionListener?.onImportRequest?.invoke()
            }
            deleteItem.setOnAction {
                val selectedCommands = this.selectionModel.selectedItems
                if (selectedCommands.size > 0) {
                    commandActionListener?.onDeleteRequest?.invoke(selectedCommands)
                }
            }


            cm.items.addAll(editItem, exportItem, importItem, SeparatorMenuItem(), deleteItem)
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

        setOnKeyPressed {
            if (it.code == KeyCode.DELETE || it.code == KeyCode.BACK_SPACE) {
                val selectedCommands = this.selectionModel.selectedItems
                if (selectedCommands.size > 0) {
                    commandActionListener?.onDeleteRequest?.invoke(selectedCommands)
                }
            }
        }
    }
}