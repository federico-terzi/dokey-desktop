package app.control_panel

import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.layout.VBox
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver

class DropDialog(parent: BlurrableStage, imageResolver: ImageResolver)
    : OverlayDialog(parent, imageResolver, enableCloseBtn = false) {

    // Custom validation logic for the drag and drop payload
    var verifyPayload: ((DragEvent) -> Boolean)? = {
        false
    }

    private val contentBox = VBox()
    private val imageView = ImageView()
    private val label = Label()

    init {
        height = 400.0

        contentBox.styleClass.add("drop-dialog-contentbox")
        contentBox.alignment = Pos.CENTER

        imageView.image = imageResolver.resolveImage("asset:dropfiles", 152)
        imageView.fitWidth = 152.0
        imageView.fitHeight = 152.0
        
        label.text = "Drop here"  // TODO: i18n

        contentBox.children.addAll(imageView, label)

        initializeUI()

        contentBox.setOnDragExited {
            onClose()
        }
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}