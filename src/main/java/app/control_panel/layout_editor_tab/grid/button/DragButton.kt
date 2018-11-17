package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import app.control_panel.layout_editor_tab.grid.dnd.ComponentDragReference
import javafx.animation.RotateTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.CacheHint
import javafx.scene.control.Button
import javafx.scene.input.TransferMode
import javafx.util.Duration
import json.JSONObject
import model.command.Command

open class DragButton(val context : GridContext) : Button() {
    var onComponentsDragEntered : ((ComponentDragReference) -> Unit)? = null
    var onComponentsDragExited : (() -> Unit)? = null

    var onComponentsDropped : ((ComponentDragReference) -> Boolean)? = null
    var onExternalResourceDropped: ((Command) -> Boolean)? = null

    var gridX : Int = -1
    var gridY : Int = -1

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

    private var _dragError = false
    var dragError : Boolean
        get() = _dragError
        set(value) {
            if (value) {
                styleClass.add("drag-error")
            } else {
                styleClass.remove("drag-error")
            }
            _dragError = value
        }

    private var _loading : Boolean = false
    var loading : Boolean
        get() = _loading
        set(value) {
            if (value) {
                this.cacheHint = CacheHint.SPEED
                val rotate = RotateTransition(Duration.seconds(5.0), this)
                rotate.toAngle = 2080.0
                rotate.play()
            } else {
                this.rotate = 0.0
            }
            _loading = value
        }

    init {
        styleClass.add("drag-button")

        onDragOver = EventHandler { event ->
            if (event.gestureSource !== this@DragButton) {
                if ((event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX)) || context.dndCommandProcessor.isCompatible(event.dragboard)) {
                    event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
                }
            }

            event.consume()
        }

        onDragEntered = EventHandler { event ->
            if (event.gestureSource !== this@DragButton) {
                if (event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX)) {  // COMPONENT MOVE
                    // Get the drop json
                    val json = event.dragboard.string.substring(COMPONENT_DRAG_PREFIX.length)  // Remove the first COMPONENT_DRAG_PREFIX string

                    // Get the component reference
                    val componentReference = ComponentDragReference.fromJson(context.componentParser, JSONObject(json))

                    onComponentsDragEntered?.invoke(componentReference)
                }else if (context.dndCommandProcessor.isCompatible(event.dragboard)) {  // EXTERNAL CONTENT
                    dragDestination = true
                }
            }

            event.consume()
        }

        onDragExited = EventHandler { event ->
            if (event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX)) {  // COMPONENT MOVE
                onComponentsDragExited?.invoke()
            }else if (context.dndCommandProcessor.isCompatible(event.dragboard)) {  // EXTERNAL CONTENT
                dragDestination = false
            }

            event.consume()
        }

        onDragDropped = EventHandler { event ->
            var success = false

            if (event.gestureSource !== this@DragButton) {
                if ((event.dragboard.hasString() && event.dragboard.string.startsWith(COMPONENT_DRAG_PREFIX))) {  // COMPONENT MOVED IN THE GRID
                    // Get the drop json
                    val json = event.dragboard.string.substring(COMPONENT_DRAG_PREFIX.length)  // Remove the first COMPONENT_DRAG_PREFIX string

                    // Get the component reference
                    val componentReference = ComponentDragReference.fromJson(context.componentParser, JSONObject(json))

                    // Notify the listener
                    success = onComponentsDropped?.invoke(componentReference) ?: false

                }else if(context.dndCommandProcessor.isCompatible(event.dragboard)) {  // EXTERNAL ELEMENT
                    loading = true

                    // Try to resolve the correct command basend on the clipboard data
                    context.dndCommandProcessor.resolve(event.dragboard) { command ->
                        if (command != null) {
                            Platform.runLater {
                                onExternalResourceDropped?.invoke(command)
                            }
                        }
                    }
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
