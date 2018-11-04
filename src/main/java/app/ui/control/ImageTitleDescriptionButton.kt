package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import system.image.ImageResolver

open class ImageTitleDescriptionButton(val imageResolver: ImageResolver, val imageId: String,
                                       val title: String, val description: String) : Button() {
    private val contentBox = HBox()
    private val imageView = ImageView()
    private val nameLabel = Label(title)
    private val descriptionLabel = Label(description)

    init {
        styleClass.add("image-title-description-button")

        nameLabel.styleClass.add("image-title-description-button-name")
        descriptionLabel.styleClass.add("image-title-description-button-desc")

        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0
        imageResolver.loadInto(imageId, 24, imageView)

        val vBox = VBox()
        vBox.children.addAll(nameLabel, descriptionLabel)

        contentBox.alignment = Pos.CENTER_LEFT
        contentBox.spacing = 6.0

        contentBox.children.addAll(imageView, vBox)

        graphic = contentBox
    }
}