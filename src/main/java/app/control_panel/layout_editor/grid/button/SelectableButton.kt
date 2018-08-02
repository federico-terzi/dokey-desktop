package app.control_panel.layout_editor.grid.button

import app.control_panel.layout_editor.grid.GridContext
import app.control_panel.layout_editor.grid.exception.CommandNotFoundException
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import model.command.Command
import model.component.Component
import system.image.ImageResolver
import utils.OSValidator
import java.util.*


open class SelectableButton(context : GridContext) : DragButton(context) {
    private var _selected = false

    var selected : Boolean
        get() = _selected
        set(value) {
            if (value) {
                styleClass.add("selected")
            } else {
                styleClass.remove("selected")
            }
            _selected = value
        }
}
