package app.ui.control

import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import system.image.ImageResolver


class StyledMenuItem(val imageResourcePath: String, text: String) : MenuItem(text) {
    init {
        val image = ImageResolver.getImage(StyledMenuItem::class.java.getResourceAsStream(imageResourcePath), 16)
        val imageView = ImageView(image)
        imageView.fitWidth = 16.0
        imageView.fitHeight = 16.0
        graphic = imageView
    }
}