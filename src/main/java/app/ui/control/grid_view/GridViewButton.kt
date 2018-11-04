package app.ui.control.grid_view

import app.ui.control.grid_view.model.GridViewEntry
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import system.image.ImageResolver

class GridViewButton(val imageResolver: ImageResolver) : Button() {
    private val imageView = ImageView()

    private var _entry : GridViewEntry? = null
    var entry : GridViewEntry?
        get() = _entry
        set(newEntry) {
            if (newEntry == null) {
                opacity = 0.0
            }else{
                opacity = 1.0

                imageResolver.loadInto(newEntry.imageId, 24, imageView)
//                imageResolver.resolveImageAsync(newEntry.imageId, 24) {image, externalThread ->
//                    if (image != null) {
//                        if (externalThread) {
//                            Platform.runLater {
//                                imageView.image = image
//                            }
//                        }else{
//                            imageView.image = image
//                        }
//                    }
//                }

                text = newEntry.name
            }

            _entry = newEntry
        }

    init {
        styleClass.add("grid-view-button")

        graphic = imageView
        contentDisplay = ContentDisplay.TOP
    }
}