package app.control_panel

import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.scene.control.Label
import javafx.scene.input.DragEvent
import javafx.scene.layout.VBox
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver

class DropDialog(parent: BlurrableStage, imageResolver: ImageResolver)
    : OverlayDialog(parent, imageResolver, enableCloseBtn = false) {

    private val contentBox = VBox()

    var verifyPayload: ((DragEvent) -> Boolean)? = {
        false
    }

    init {
        height = 400.0

        contentBox.children.add(Label("Drop here"))

        initializeUI()

        contentBox.setOnDragExited {
            onClose()
        }
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}