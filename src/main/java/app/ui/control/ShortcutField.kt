package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane

class ShortcutField : StackPane() {
    private val textField = TextField()
    private val clearButton = Button("Clear")  // TODO: i18n

    val keys = mutableListOf<String>()

    init {
        styleClass.add("shortcut-field")
        alignment = Pos.TOP_RIGHT

        textField.styleClass.add("shortcut-field-text")
        clearButton.styleClass.add("shortcut-field-clear-button")

        textField.alignment = Pos.CENTER
        textField.promptText = "Click here and type the shortcut..."  // TODO: i18n

        children.addAll(textField, clearButton)

        clearButton.setOnAction {
            keys.clear()
            render()
        }

        textField.isEditable = false

        textField.addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            addKey(keyEvent.code.name.toUpperCase())

            keyEvent.consume()
        }
    }

    fun addKey(key: String) {
        val keyName = convertKeyName(key)

        if (!keys.contains(keyName)) {
            keys.add(keyName)
        }

        render()
    }

    fun loadShortcut(shortcut: String) {
        keys.clear()
        keys.addAll(shortcut.split("+"))
        render()
    }

    fun getShortcut() : String? {
        if (!textField.text.isBlank()) {
            return textField.text
        }else{
            return null
        }
    }

    private fun render() {
        textField.text = keys.joinToString(separator = "+")
        textField.positionCaret(textField.text.length)
    }

    companion object {
        val commonConversionMap = mapOf<String, String>(
                "CONTROL" to "CTRL",
                "WINDOWS" to "WIN"
        )

        fun convertKeyName(rawKeyName: String) : String {
            val converted = commonConversionMap[rawKeyName]
            if (converted != null){
                return converted
            }

            // No conversion needed
            return rawKeyName
        }
    }
}