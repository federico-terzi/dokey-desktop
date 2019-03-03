package app.control_panel.dialog.command_edit_dialog.builder

import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.RoundBorderButton
import app.ui.control.StyledComboBox
import app.ui.control.StyledTextField
import app.ui.stage.BlurrableStage
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.command.Command
import system.commands.specific.PhotoshopSliderCommand
import system.external.photoshop.PhotoshopSliderAction

@RegisterBuilder(type = PhotoshopSliderCommand::class)
class PhotoshopSliderBuilder(val context: BuilderContext, val parent: BlurrableStage) : CommandBuilder {
    override val contentBox = VBox()
    private val typeComboBox = StyledComboBox<PhotoshopSliderAction>()
    private val minTextField = StyledTextField()
    private val maxTextField = StyledTextField()
    private val resetBtn = RoundBorderButton("Reset")  // TODO: i18n

    private var defaultMin: Float = 0f
    private var defaultMax: Float = 100f

    init {
        contentBox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        contentBox.spacing = 7.0
        contentBox.alignment = Pos.CENTER

        val hBox = HBox()
        hBox.spacing = 7.0
        resetBtn.minWidth = 50.0
        hBox.children.addAll(minTextField, maxTextField, resetBtn)

        contentBox.children.addAll(typeComboBox, hBox)

        typeComboBox.promptText = "Select Type..."  // TODO: i18n
        typeComboBox.items = FXCollections.observableArrayList(PhotoshopSliderAction.values().toList())

        resetBtn.setOnAction {
            minTextField.text = defaultMin.toString()
            maxTextField.text = defaultMax.toString()
        }
    }

    override fun populateUIForCommand(command: Command) {
        command as PhotoshopSliderCommand

        typeComboBox.selectionModel.select(command.slider)

        defaultMin = command.defaultMin!!
        defaultMax = command.defaultMax!!

        minTextField.text = command.min!!.toString()
        maxTextField.text = command.max!!.toString()

        // If command is locked then disable the button
        if (command.locked) {
            contentBox.isDisable = true
        }
    }

    override fun updateCommand(command: Command) {
        command as PhotoshopSliderCommand

        command.slider = typeComboBox.value

        command.min = minTextField.text.toFloat()
        command.max = maxTextField.text.toFloat()
    }

    override fun validateInput() {
        if (typeComboBox.value == null) {
            throw ValidationException("Please choose a Photoshop slider type") // TODO: i18n
        }

        if (minTextField.text.isBlank() || maxTextField.text.isBlank()) {
            throw ValidationException("Value range cannot be empty") // TODO: i18n
        }

        try {
            val min = minTextField.text.trim().toFloat()
            val max = maxTextField.text.trim().toFloat()

            if (min < defaultMin || max > defaultMax || min >= max) {
                throw ValidationException("Value range is not valid.") // TODO: i18n
            }
        }catch (e: NumberFormatException) {
            throw ValidationException("The given range values are not numbers.")
        }
    }
}