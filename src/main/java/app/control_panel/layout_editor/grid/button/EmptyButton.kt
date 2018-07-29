package app.control_panel.layout_editor.grid.button

import app.control_panel.layout_editor.grid.GridContext
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ContextMenu
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import system.image.ImageResolver

class EmptyButton(context : GridContext) : DragButton(context) {
    var onEmptyButtonClicked : (() -> Unit)? = null

    init {
        // Set up the button design
        styleClass.add("empty-btn")
        val image = ImageResolver.getImage(EmptyButton::class.java.getResourceAsStream("/assets/add_clean.png"), 24)
        val imageView = ImageView(image)
        imageView.setFitWidth(24.0)
        imageView.setFitHeight(24.0)
        graphic = imageView
        contentDisplay = ContentDisplay.TOP

        // Set up the context menu
        val contextMenu = ContextMenu()
        // TODO
        setContextMenu(contextMenu)

        // Set up the click listener
        setOnMouseClicked {
            onEmptyButtonClicked?.let { it() }
        }
    }
}
