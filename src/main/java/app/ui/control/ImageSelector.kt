package app.ui.control

import app.control_panel.dialog.image_select_dialog.ImageSelectDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import system.image.ImageResolver

class ImageSelector(val parent: BlurrableStage, val imageResolver: ImageResolver) : StackPane() {
    private val button = Button()
    private val imageView = ImageView()
    private val pencilImage = ImageView()

    private var _imageId : String? = null
    var imageId : String?
        get() = _imageId
        set(value) {
            val id = value ?: "asset:cmd_icon_default"
            imageView.image = imageResolver.resolveImage(id, 70)
            _imageId = value
        }

    init {
        styleClass.add("image-selector")
        maxWidth = 70.0

        imageId = null
        imageView.fitWidth = 70.0
        imageView.fitHeight = 70.0

        button.styleClass.add("image-selector-button")
        button.graphic = imageView

        pencilImage.fitWidth = 20.0
        pencilImage.fitHeight = 20.0
        imageResolver.loadInto("asset:cmd_icon_edit", 20, pencilImage)

        children.addAll(button, pencilImage)

        alignment = Pos.TOP_RIGHT

        button.setOnAction {
            selectImage()
        }
        pencilImage.setOnMouseClicked { selectImage() }
    }

    private fun selectImage() {
        val dialog = ImageSelectDialog(parent, imageResolver)
        dialog.onImageSelected = {id ->
            imageId = id
        }
        dialog.showWithAnimation()
    }
}