package app.ui.control

import javafx.animation.RotateTransition
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import javafx.util.Duration
import system.image.ImageResolver

class SaveButton(val imageResolver: ImageResolver, val title: String) : Button() {
    private val imageView : ImageView

    init {
        styleClass.add("save-button")

        val image = imageResolver.resolveImage("asset:check-circle", 18)
        imageView = ImageView(image)
        imageView.fitHeight = 18.0
        imageView.fitWidth = 18.0

        graphic = imageView
        text = title

        alignment = Pos.CENTER

        contentDisplay = ContentDisplay.LEFT
    }
}