package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import app.ui.popup.StyledPopup
import javafx.scene.CacheHint
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ContextMenu
import javafx.scene.image.ImageView
import system.image.ImageResolver

class EmptyButton(context : GridContext) : SelectableButton(context) {
    init {
        // Set up the button design
        styleClass.add("empty-btn")

        val imageView = ImageView()
        imageView.fitWidth = 48.0
        imageView.fitHeight = 48.0
        context.imageResolver.loadInto("asset:emptybtn", 48, imageView, fadeIn = false)

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
