package app.control_panel

import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.VBox
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver

class DropDialog(parent: BlurrableStage, imageResolver: ImageResolver)
    : OverlayDialog(parent, imageResolver, enableCloseBtn = false) {

    // Custom validation logic for the drag and drop payload
    var verifyPayload: ((DragEvent) -> Boolean)? = {
        false
    }

    var onContentDropped : ((Dragboard) -> Unit)? = null

    private val contentBox = VBox()
    private val imageView = ImageView()
    private val label = Label()

    init {
        height = 400.0

        contentBox.styleClass.add("drop-dialog-contentbox")
        contentBox.alignment = Pos.CENTER

        imageView.image = imageResolver.resolveImage("asset:dropfiles", 128)
        imageView.fitWidth = 128.0
        imageView.fitHeight = 128.0
        
        label.text = "Drop here"  // TODO: i18n

        contentBox.children.addAll(imageView, label)

        initializeUI()

        contentBox.setOnDragExited {
            onClose()
        }
        contentBox.setOnDragOver {
            if (verifyPayload?.invoke(it) == true) {
                it.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
            }

            it.consume()
        }
        contentBox.setOnDragDropped {
            if (verifyPayload?.invoke(it) == true) {
                onContentDropped?.invoke(it.dragboard)
            }

            it.consume()
        }
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}