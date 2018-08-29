package app.control_panel.tab_selector

import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import system.image.ImageResolver

class Tab(val imageResolver: ImageResolver, val tabLabel : String, val tabImage : String) : Button() {
    init {
        val vBox = VBox()
        val label = Label(tabLabel)
        val image = imageResolver.resolveImage(tabImage, 24)
        val imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        vBox.children.addAll(imageView, label)

        graphic = vBox
        contentDisplay = ContentDisplay.TOP
    }
}