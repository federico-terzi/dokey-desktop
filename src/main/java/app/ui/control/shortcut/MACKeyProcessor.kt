package app.ui.control.shortcut

import com.sun.jna.Callback
import javafx.application.Platform
import javafx.scene.input.KeyEvent
import system.keyboard.bindings.MacKeyboardLib

/**
 * On Mac, we need to remove the modifier keys from the requested key text and extract the original key.
 * For example, if someone clicks on ALT + è in an italian keyboard, on mac the result would be "SHIFT + ["
 * We need to decompose the "[" by removing the modifier keys ( ALT ) and extract "è"
 */
class MACKeyProcessor : KeyProcessor() {
    private var macRemoveKeyModifiersCallback = MacKeyboardLib.RemoveModifiersFromKeyCallback { key ->
        Platform.runLater {
            onTextKeyPressed?.invoke(key)
        }
    }

    override fun requestTextKeyPressed(keyEvent: KeyEvent) {
        val control = if (keyEvent.isControlDown) 1 else 0
        val shift = if (keyEvent.isShiftDown) 1 else 0
        val command = if (keyEvent.isMetaDown) 1 else 0
        val alt = if (keyEvent.isAltDown) 1 else 0
        MacKeyboardLib.INSTANCE.removeModifiersFromKey(keyEvent.text, control, alt, shift, command, macRemoveKeyModifiersCallback)
    }
}