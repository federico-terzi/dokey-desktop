package app.ui.control

import javafx.scene.control.Button
import javafx.scene.image.ImageView
import system.image.ImageResolver

class IconButton(val imageResolver: ImageResolver, initialImageId : String, val imageSize: Int) : Button() {
    private val imageView = ImageView()

    private var _imageId: String? = null
    var imageId : String?
        get() = _imageId
        set(value) {
            if (value != null) {
                imageView.image = imageResolver.resolveImage(value, imageSize)
            }
            _imageId = value
        }


    init {
        styleClass.add("icon-button")

        imageView.fitHeight = imageSize.toDouble()
        imageView.fitWidth = imageSize.toDouble()
        graphic = imageView

        imageId = initialImageId
    }
}