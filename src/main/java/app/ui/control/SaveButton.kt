package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import system.image.ImageResolver

class SaveButton(imageResolver: ImageResolver, title: String) : StyledButton(imageResolver, title, "asset:check-circle") {
    init {
        styleClass.add("save-button")
    }
}