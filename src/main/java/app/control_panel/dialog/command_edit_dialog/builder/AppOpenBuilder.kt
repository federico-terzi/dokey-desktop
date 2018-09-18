package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.ApplicationSelectButton
import app.ui.stage.BlurrableStage
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import model.command.Command
import system.applications.Application
import system.commands.general.AppOpenCommand

@RegisterBuilder(type = AppOpenCommand::class)
class AppOpenBuilder(val context: BuilderContext, val parent: BlurrableStage) : CommandBuilder {
    override val contentBox = VBox()
    private val applicationButton = ApplicationSelectButton(parent, context.imageResolver,
            context.applicationManager, allowGlobal = false)

    init {
        contentBox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        contentBox.alignment = Pos.CENTER

        contentBox.children.addAll(applicationButton)
    }

    override fun populateUIForCommand(command: Command) {
        command as AppOpenCommand

        val application = if (command.executablePath != null ) {
            context.applicationManager.getApplication(command.executablePath)
        }else{
            null
        }

        applicationButton.application = application
    }

    override fun updateCommand(command: Command) {
        command as AppOpenCommand

        command.executablePath = applicationButton.application?.executablePath

        // Override the icon if null
        if (command.iconId == null) {
            command.iconId = "app:${applicationButton.application?.executablePath}"
        }
    }

    override fun validateInput() {
        if (applicationButton.application == null) {
            throw ValidationException("Please select an Application.") // TODO: i18n
        }
    }
}