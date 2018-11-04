package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import app.ui.popup.StyledPopup
import javafx.scene.CacheHint
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ContextMenu
import javafx.scene.image.ImageView
import system.image.ImageResolver

class EmptyButton(context : GridContext) : DragButton(context) {
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

        isCache = true
        cacheHint = CacheHint.SPEED
    }
}
