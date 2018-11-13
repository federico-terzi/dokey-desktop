package app.ui.control.shortcut

import javafx.scene.input.KeyEvent

class MSKeyProcessor : KeyProcessor() {
    override fun requestTextKeyPressed(keyEvent: KeyEvent) {
        onTextKeyPressed?.invoke(keyEvent.text)
    }
}