package app.control_panel.dialog.command_edit_dialog.builder

import app.ui.control.IconButton
import app.ui.control.StyledLabel
import app.ui.control.StyledTextField
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

abstract class ImageEditableTextCommandBuilder(val context: BuilderContext, val imageId : String) : CommandBuilder {
    override val contentBox = VBox()
    protected val imageView = IconButton(context.imageResolver, imageId, 24)
    protected val textField = StyledTextField()

    init {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        HBox.setHgrow(textField, Priority.ALWAYS)

        hBox.children.addAll(imageView, textField)

        contentBox.children.add(hBox)
    }
}