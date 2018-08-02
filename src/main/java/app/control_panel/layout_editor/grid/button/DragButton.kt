package app.control_panel.layout_editor.grid.button

import app.control_panel.layout_editor.grid.GridContext
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.input.TransferMode
import json.JSONObject
import model.component.Component

open class DragButton(val context : GridContext) : Button() {

    var onComponentDropped : ((Component) -> Boolean)? = null

    private var _dragDestination = false
    var dragDestination : Boolean
        get() = _dragDestination
        set(value) {
            if (value) {
                styleClass.add("drag-entered")
            } else {
                styleClass.remove("drag-entered")
            }
            _dragDestination = value
        }

    init {
        styleClass.add("drag-button")

        onDragOver = EventHandler { event ->
            if (event.gestureSource !== this@DragButton) {
                if ((event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX)) || context.dndCommandProcessor.isCompatible(event.dragboard)) {
                    event.acceptTransferModes(TransferMode.MOVE)
                }
            }

            event.consume()
        }

        onDragEntered = EventHandler { event ->
            if (event.gestureSource !== this@DragButton) {
                if ((event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX)) || context.dndCommandProcessor.isCompatible(event.dragboard)) {
                    dragDestination = true
                }
            }

            event.consume()
        }

        onDragExited = EventHandler { event ->
            dragDestination = false

            event.consume()
        }

        onDragDropped = EventHandler { event ->
            var success = false

            if (event.gestureSource !== this@DragButton) {
                if ((event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX))) {  // COMPONENT MOVED IN THE GRID
                    // Get the drop json
                    val json = event.dragboard.string.substring(COMPONENT_DRAG_PREFIX.length)  // Remove the first COMPONENT_DRAG_PREFIX string

                    // Notify the listener
                    onComponentDropped?.let {
                        // Create the component
                        val component = context.componentParser.fromJSON(JSONObject(json))
                        success = it(component)
                    }
                }else if(context.dndCommandProcessor.isCompatible(event.dragboard)) {  // EXTERNAL ELEMENT
                    // TODO
                }
            }

            event.isDropCompleted = success

            event.consume()
        }
    }

    companion object {
        val COMPONENT_DRAG_PREFIX = "DOKEY_DRAG_COMPONENT"
    }
}
