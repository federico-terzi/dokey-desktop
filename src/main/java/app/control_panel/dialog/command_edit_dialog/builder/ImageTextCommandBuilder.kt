package app.control_panel.dialog.command_edit_dialog.builder

import app.ui.control.IconButton
import app.ui.control.StyledLabel
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

abstract class ImageTextCommandBuilder(val context: BuilderContext, val imageId : String) : CommandBuilder {
    override val contentBox = VBox()
    protected val imageView = IconButton(context.imageResolver, imageId, 24)
    protected val label = StyledLabel()

    init {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        HBox.setHgrow(label, Priority.ALWAYS)

        hBox.children.addAll(imageView, label)

        contentBox.children.add(hBox)
    }
}