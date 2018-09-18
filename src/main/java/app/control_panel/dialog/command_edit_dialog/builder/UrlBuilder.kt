package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.stage.BlurrableStage
import javafx.scene.input.TransferMode
import javafx.stage.FileChooser
import model.command.Command
import system.commands.general.UrlCommand
import java.io.File
import java.net.URL

@RegisterBuilder(type = UrlCommand::class)
class UrlBuilder(context: BuilderContext, val parent: BlurrableStage) : ImageEditableTextCommandBuilder(context, "asset:link") {
    init {
        textField.promptText = "Insert the URL or drop a link here..."  // TODO: i18n

        // Setup drag and drop
        textField.setOnDragOver{ event ->
            if ((event.dragboard.hasString() && event.dragboard.string.startsWith("http")) ||
                    event.dragboard.hasUrl()) {
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
            }

            event.consume()
        }
        textField.setOnDragDropped { event ->
            var success = false

            if ((event.dragboard.hasString() && event.dragboard.string.startsWith("http")) ||
                    event.dragboard.hasUrl()) {

                if (event.dragboard.url != null) {
                    textField.text = event.dragboard.url
                }else{
                    textField.text = event.dragboard.string
                }

                success = true
            }

            event.isDropCompleted = success

            event.consume()
        }
    }

    override fun populateUIForCommand(command: Command) {
        command as UrlCommand

        textField.text = command.url
    }

    override fun updateCommand(command: Command) {
        command as UrlCommand

        command.url = textField.text
    }

    override fun validateInput() {
        if (textField.text.isBlank()) {
            throw ValidationException("Please insert the web link.") // TODO: i18n
        }

        // Validate the link
        try {
            var url = textField.text
            if (url.contains(".") && !url.startsWith("http")) {
                url = "http://$url"
            }

            val u = URL(url)
            u.toURI()
        }catch (e: Exception) {
            throw ValidationException("The inserted link is not valid.") // TODO: i18n
        }
    }


}