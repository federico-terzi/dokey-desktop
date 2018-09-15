package app.ui.list_cell

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import system.image.ImageResolver

data class ImageTextEntry(val imageId: String, val title: String)

class ImageTextListCell<T>(val imageResolver: ImageResolver,
                           val adapter: (T) -> ImageTextEntry) : ListCell<T>() {
    private val imageView : ImageView
    private val titleLabel : Label
    private val hBox = HBox()

    init {
        styleClass.add("image-text-list-cell")

        val image = imageResolver.resolveImage("asset:image", 24)
        imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        titleLabel = Label();

        hBox.children.addAll(imageView, titleLabel)
        hBox.alignment = Pos.CENTER_LEFT
        graphic = hBox
    }

    private fun addContent(current: T) {
        val entry = adapter(current)

        titleLabel.text = entry.title

        imageResolver.resolveImageAsync(entry.imageId, 24) {image, externalThread ->
            if (image != null) {
                if (externalThread) {
                    Platform.runLater {
                        imageView.image = image
                    }
                }else{
                    imageView.image = image
                }
            }
        }

        graphic = hBox
    }

    override fun updateItem(result: T?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}