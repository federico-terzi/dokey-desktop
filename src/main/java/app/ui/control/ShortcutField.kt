package app.ui.control

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import system.keyboard.bindings.MacKeyboardLib
import system.keyboard.bindings.WinKeyboardLib
import utils.OSValidator

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
            // Check if the key contains text
            if (!keyEvent.text.isBlank()) {
                // Make sure the keyEvent is not a numpad digit key
                if (keyEvent.code?.name?.toUpperCase()?.startsWith("NUMPAD") == false) {
                    addKey(keyEvent.text)
                }else{
                    addSpecialKey(keyEvent.code.name.toUpperCase())
                }
            }else{  // The key does not produce any text, so use the code name
                // Make sure the pressed key was not caps lock
                if (keyEvent.code.name.toUpperCase() != "CAPS") {
                    addSpecialKey(keyEvent.code.name.toUpperCase())
                }else{  // The user pressed the caps lock, disable it
                    disableCapsLock()
                }
            }

            keyEvent.consume()
        }

        // When the text field is focused, automatically disable CAPS LOCK key
        textField.focusedProperty().addListener { _, _, focused ->
            if (focused) {
                disableCapsLock()
            }
        }
    }

    fun addKey(key: String) {
        // If the key is +, convert it to PLUS to differentiate it from the + separator
        val finalKey = if (key == "+") {
            "PLUS"
        }else{
            key
        }

        if (!keys.contains(finalKey)) {
            keys.add(finalKey)
        }

        render()
    }

    fun addSpecialKey(key: String) {
        val keyName = convertKeyName(key)

        // Filter out undefined keys
        if (keyName == "UNDEFINED") {
            return
        }

        // ALT_GRAPH is a shortcut to CTRL+ALT, and we want to filter it out.
        // The problem is that when a user presses ALT_GRAPH, the keyboard first
        // presses CTRL and then ALT_GRAPH, so as a workaround, if we receive an
        // ALT_GRAPH key, we ignore it but also remove a previous CTRL if present
        if (key == "ALT_GRAPH") {
            if (keys.contains("CTRL")) {
                keys.remove("CTRL")
                render()
            }
            return
        }

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
                "WINDOWS" to "WIN",
                "COMMAND" to "CMD",
                "DIGIT1" to "1",
                "DIGIT2" to "2",
                "DIGIT3" to "3",
                "DIGIT4" to "4",
                "DIGIT5" to "5",
                "DIGIT6" to "6",
                "DIGIT7" to "7",
                "DIGIT8" to "8",
                "DIGIT9" to "9",
                "DIGIT0" to "0"
        )

        fun convertKeyName(rawKeyName: String) : String {
            val converted = commonConversionMap[rawKeyName]
            if (converted != null){
                return converted
            }

            // No conversion needed
            return rawKeyName
        }

        fun disableCapsLock() {
            if (OSValidator.isWindows()) {
                WinKeyboardLib.INSTANCE.forceDisableCapsLock()
            }else if (OSValidator.isMac()) {
                MacKeyboardLib.INSTANCE.forceDisableCapsLock()
            }
        }
    }
}