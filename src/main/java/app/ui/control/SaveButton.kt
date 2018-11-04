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

        imageView = ImageView()
        imageView.fitHeight = 18.0
        imageView.fitWidth = 18.0
        imageResolver.loadInto("asset:check-circle", 18, imageView)

        graphic = imageView
        text = title

        alignment = Pos.CENTER

        contentDisplay = ContentDisplay.LEFT
    }
}