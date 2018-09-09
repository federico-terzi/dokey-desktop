package app.ui.control

import javafx.scene.control.Button
import javafx.scene.image.ImageView
import system.image.ImageResolver

class IconButton(val imageResolver: ImageResolver, val imageId : String, val imageSize: Int) : Button() {
    init {
        styleClass.add("icon-button")

        val image = imageResolver.resolveImage(imageId, imageSize)
        val imageView = ImageView(image)
        imageView.fitHeight = imageSize.toDouble()
        imageView.fitWidth = imageSize.toDouble()
        graphic = imageView

    }
}