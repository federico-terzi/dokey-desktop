package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import system.image.ImageResolver

class DeleteButton(imageResolver: ImageResolver, title: String) : StyledButton(imageResolver, title, "asset:delete") {
    init {
        styleClass.add("delete-button")
    }
}