package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import system.image.ImageResolver

class ImageSelector(val imageResolver: ImageResolver) : StackPane() {
    private val button = Button()
    private val imageView = ImageView()
    private val pencilImage = ImageView()

    var image : Image
        get() = imageView.image
        set(value) {imageView.image = value}

    init {
        styleClass.add("image-selector")
        maxWidth = 70.0

        imageView.image = imageResolver.resolveImage("asset:cmd_icon_default", 70)
        imageView.fitWidth = 70.0
        imageView.fitHeight = 70.0

        button.styleClass.add("image-selector-button")
        button.graphic = imageView

        pencilImage.image = imageResolver.resolveImage("asset:cmd_icon_edit", 20)
        pencilImage.fitWidth = 20.0
        pencilImage.fitHeight = 20.0

        children.addAll(button, pencilImage)

        alignment = Pos.TOP_RIGHT
    }
}