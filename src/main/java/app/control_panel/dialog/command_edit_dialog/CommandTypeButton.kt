package app.control_panel.dialog.command_edit_dialog

import app.control_panel.dialog.command_type_dialog.CommandTypeDialog
import app.control_panel.dialog.command_type_dialog.CommandTypeListView
import app.ui.stage.BlurrableStage
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import model.command.Command
import system.commands.CommandDescriptor
import system.image.ImageResolver

class CommandTypeButton(val parent: BlurrableStage, val imageResolver: ImageResolver) : Button() {
    private val imageView = ImageView()

    var commandDescriptor : CommandDescriptor? = null
    var onTypeSelected : ((CommandDescriptor) -> Unit)? = null

    init {
        styleClass.add("command-type-button")

        imageView.image = imageResolver.resolveImage("asset:sort", 12)
        imageView.fitWidth = 12.0
        imageView.fitHeight = 12.0

        text = "Select type" // TODO: i18n

        graphic = imageView
        contentDisplay = ContentDisplay.RIGHT

        setOnAction {
            val dialog = CommandTypeDialog(parent, imageResolver)
            dialog.onTypeSelected = {commandDescriptor ->
                if (commandDescriptor != null) {
                    this.commandDescriptor = commandDescriptor
                    onTypeSelected?.invoke(commandDescriptor)

                    text = commandDescriptor.title
                }
            }
            dialog.showWithAnimation()
        }
    }

    fun selectTypeForCommand(command: Command) {
        val descriptor = CommandTypeListView.commandDescriptors.find {
            it.associatedCommandClass == command::class.java
        }
        if (descriptor != null) {
            text = descriptor.title
        }
    }
}