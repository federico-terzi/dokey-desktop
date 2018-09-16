package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.ui.control.IconButton
import app.ui.control.StyledTextField
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.general.FolderOpenCommand

@RegisterBuilder(type = FolderOpenCommand::class)
class FolderOpenBuilder(val context: BuilderContext) : CommandBuilder {
    override val contentBox = VBox()
    private val folderImage = IconButton(context.imageResolver, "asset:folder", 24)
    private val pathField = StyledTextField()

    init {
        val hBox = HBox()
        hBox.alignment = Pos.CENTER
        HBox.setHgrow(pathField, Priority.ALWAYS)

        pathField.isDisable = true
        pathField.text = "Drop folder here or click to select..."  // TODO: i18n

        hBox.children.addAll(folderImage, pathField)

        contentBox.children.add(hBox)
    }

    override fun populateUIForCommand(command: Command) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCommand(command: Command) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}