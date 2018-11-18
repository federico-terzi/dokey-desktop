package app.ui.control

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import system.image.ImageResolver

class IconButton(val imageResolver: ImageResolver, initialImageId : String, val imageSize: Int,
                 noPadding: Boolean = false) : Button() {
    private val imageView = ImageView()

    private var _imageId: String? = null
    var imageId : String?
        get() = _imageId
        set(value) {
            if (value != null) {
                imageResolver.loadInto(value, imageSize, imageView)
            }
            _imageId = value
        }


    init {
        styleClass.add("icon-button")

        if (noPadding) {
            padding = Insets(0.0,0.0,0.0,0.0)
        }else{
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }

        imageView.fitHeight = imageSize.toDouble()
        imageView.fitWidth = imageSize.toDouble()
        graphic = imageView

        imageId = initialImageId
    }
}