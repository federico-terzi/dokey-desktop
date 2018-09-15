package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

class ColonTextField() : HBox() {
    private val colonLabel = Label()
    private val textField = TextField()

    init {
        styleClass.add("colon-text-field")

        alignment = Pos.CENTER_LEFT

        textField.promptText = "Quick Start..."  // TODO: make international
        HBox.setHgrow(textField, Priority.ALWAYS)

        colonLabel.styleClass.add("colon-text-field-label")
        colonLabel.text = ":"

        // Disable non-alfanumeric chars
        textField.textProperty().addListener { _, oldValue, newValue ->
            if (!newValue.isBlank() && newValue.contains(Regex("[^A-Za-z0-9]"))) {
                textField.text = oldValue
            }
        }

        children.addAll(colonLabel, textField)
    }
}