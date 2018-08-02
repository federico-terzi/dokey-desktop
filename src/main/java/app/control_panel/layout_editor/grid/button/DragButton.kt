package app.control_panel.layout_editor.grid.button

import app.control_panel.layout_editor.grid.GridContext
import javafx.event.EventHandler
import javafx.scene.ImageCursor
import javafx.scene.control.Button
import javafx.scene.input.TransferMode
import json.JSONObject
import model.component.Component

open class DragButton(val context : GridContext) : Button() {

    var onComponentDragListener: OnComponentDragListener? = null

    init {
        styleClass.add("drag-button")

        onDragOver = EventHandler { event ->
            if (event.gestureSource !== this@DragButton &&
                    event.dragboard.hasString() && event.dragboard.string.startsWith(DRAG_PREFIX)) {
                event.acceptTransferModes(TransferMode.MOVE)
            }

            event.consume()
        }

        onDragEntered = EventHandler { event ->
            if (event.gestureSource !== this@DragButton &&
                    event.dragboard.hasString() && event.dragboard.string.startsWith(DRAG_PREFIX)) {
                // Get the drop json
                val json = event.dragboard.string.substring(DRAG_PREFIX.length)  // Remove the first DRAG_PREFIX string

                // Notify the listener
                if (onComponentDragListener != null) {
                    // Create the component
                    val component = context.componentParser.fromJSON(JSONObject(json))
                    onComponentDragListener!!.onComponentDropping(component)
                }
            }

            event.consume()
        }

        onDragExited = EventHandler { event ->
            if (event.gestureSource !== this@DragButton &&
                    event.dragboard.hasString() && event.dragboard.string.startsWith(DRAG_PREFIX)) {
            }

            setDragDestination(false, false)

            event.consume()
        }

        onDragDropped = EventHandler { event ->
            var success = false

            if (event.gestureSource !== this@DragButton &&
                    event.dragboard.hasString() && event.dragboard.string.startsWith(DRAG_PREFIX)) {
                // Get the drop json
                val json = event.dragboard.string.substring(DRAG_PREFIX.length)  // Remove the first DRAG_PREFIX string

                // Notify the listener
                if (onComponentDragListener != null) {
                    // Create the component
                    val component = context.componentParser.fromJSON(JSONObject(json))
                    success = onComponentDragListener!!.onComponentDropped(component)
                }
            }

            event.isDropCompleted = success

            event.consume()
        }
    }

    fun setDragDestination(dragDestination: Boolean, swapping: Boolean) {
        if (dragDestination) {
            if (!swapping) {
                styleClass.add("drag-entered")
            } else {
                styleClass.add("drag-entered-swapping")
            }
        } else {
            styleClass.remove("drag-entered")
            styleClass.remove("drag-entered-swapping")
        }
    }

    interface OnComponentDragListener {
        fun onComponentDropped(component: Component): Boolean
        fun onComponentDropping(component: Component): Boolean
    }

    companion object {
        val DRAG_PREFIX = "DRAG_COMPONENT"
    }
}
