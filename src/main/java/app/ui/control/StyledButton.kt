package app.ui.control

import javafx.animation.RotateTransition
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import javafx.util.Duration
import system.image.ImageResolver

open class StyledButton(val imageResolver: ImageResolver, val title: String, val imageId: String) : Button() {
    private val imageView : ImageView

    init {
        styleClass.add("styled-button")

        imageView = ImageView()
        imageView.fitHeight = 18.0
        imageView.fitWidth = 18.0
        imageResolver.loadInto(imageId, 18, imageView)

        graphic = imageView
        text = title

        alignment = Pos.CENTER

        contentDisplay = ContentDisplay.LEFT
    }
}