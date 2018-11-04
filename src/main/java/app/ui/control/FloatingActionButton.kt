package app.ui.control

import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import system.image.ImageResolver

class FloatingActionButton(imageResolver: ImageResolver, text: String) : Button(text) {
    init {
        styleClass.add("floating-action-button")

        val imageView = ImageView()
        imageView.fitWidth = 20.0
        imageView.fitHeight = 20.0
        imageResolver.loadInto("asset:add_clean", 20, imageView)
        graphic = imageView
        contentDisplay = ContentDisplay.LEFT

        // TODO: add mouse over animation
    }
}