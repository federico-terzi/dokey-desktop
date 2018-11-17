package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import app.control_panel.layout_editor_tab.grid.exception.CommandNotFoundException
import app.control_panel.layout_editor_tab.grid.dnd.ComponentDragReference
import javafx.event.EventHandler
import javafx.scene.CacheHint
import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import model.command.Command
import model.component.Component
import system.image.ImageResolver
import utils.OSValidator
import java.util.*


class ComponentButton(context : GridContext, val associatedComponent : Component) : SelectableButton(context) {
    // Used to request to the component grid which components are selected
    var requestSelectedComponentReference: (() -> ComponentDragReference)? = null

    var onComponentActionListener: OnComponentActionListener? = null

    init {
        // Get the associated command
        val command : Command? = context.commandResolver.getCommand(associatedComponent.commandId!!)

        if (command == null) {
            throw CommandNotFoundException()
        }

        // Set up the button image and text
        text = command.title

        // Create the tooltip
        command.description?.let {
            val tooltip = Tooltip()
            tooltip.text = it
            setTooltip(tooltip)
        }

        // Set up the image
        val imageView = ImageView()
        imageView.fitHeight = 48.0
        imageView.fitWidth = 48.0
        graphic = imageView
        contentDisplay = ContentDisplay.TOP

        context.imageResolver.loadInto(command.iconId, 48, imageView, fadeIn = false)

        // Add the style
        styleClass.add("component-btn")
        if (!command.isValid!!) {  // Invalid item rendering
            styleClass.add("invalid-btn")
        }

        // Setup the context menu
        val contextMenu = ContextMenu()
        val items = ArrayList<MenuItem>()
        getContextMenu(items)
        contextMenu.items.addAll(items)
        setContextMenu(contextMenu)

        // Set the drag and drop
        onDragDetected = EventHandler { event ->
            // Get the selected components
            var componentReference = requestSelectedComponentReference?.invoke()

            // If the dragged component is not in the selection, request to deselect all the others
            // and select only the current one. Then, request again the selected components
            if (associatedComponent !in componentReference?.components ?: emptyList()) {
                onDeselectAllRequest?.invoke()
                selected = true
                componentReference = requestSelectedComponentReference?.invoke()
            }

            if (componentReference != null) {


                // TODO: change image drag when there are multiple elements
                val db = startDragAndDrop(TransferMode.MOVE)

                val sp = SnapshotParameters()
                sp.fill = Color.TRANSPARENT
                val snapshot = snapshot(sp, null)

                var offsetX = 0.0
                var offsetY = 0.0

                if (OSValidator.isWindows()) {
                    offsetX = snapshot.width / 2
                    offsetY = snapshot.height / 2
                }

                db.setDragView(snapshot, offsetX, offsetY)

                val content = ClipboardContent()
                val payload = componentReference.json()
                content.putString(DragButton.Companion.COMPONENT_DRAG_PREFIX + payload.toString())
                db.setContent(content)
            }

            event.consume()
        }
        onDragDone = EventHandler { event ->
            if (event.transferMode == TransferMode.MOVE) {
                if (onComponentActionListener != null) {
                    onComponentActionListener!!.onComponentDroppedAway()
                }
            }

            cursor = Cursor.DEFAULT

            event.consume()
        }

        isCache = true
        cacheHint = CacheHint.SPEED
    }

    /**
     * Populate the context menu
     * @param items
     */
    protected fun getContextMenu(items: MutableList<MenuItem>) {
        val edit = MenuItem(context.resourceBundle.getString("edit"))
        edit.onAction = EventHandler {
            if (onComponentActionListener != null) {
                onComponentActionListener!!.onComponentEdit()
            }
        }

        val editImage = ImageResolver.getImage(ComponentButton::class.java.getResourceAsStream("/assets/edit.png"), 16)
        val editImageView = ImageView(editImage)
        editImageView.setFitWidth(16.0)
        editImageView.setFitHeight(16.0)
        edit.graphic = editImageView

        val delete = MenuItem(context.resourceBundle.getString("delete"))
        delete.onAction = EventHandler {
            if (onComponentActionListener != null) {
                onComponentActionListener!!.onComponentDelete()
            }
        }
        val deleteImage = ImageResolver.getImage(ComponentButton::class.java.getResourceAsStream("/assets/delete.png"), 16)
        val deleteImageView = ImageView(deleteImage)
        deleteImageView.setFitWidth(16.0)
        deleteImageView.setFitHeight(16.0)
        delete.graphic = deleteImageView

        items.add(edit)
        items.add(delete)
    }

    interface OnComponentActionListener {
        fun onComponentEdit()
        fun onComponentDelete()
        fun onComponentDroppedAway()
    }
}
