package app.control_panel.layout_editor

import javafx.scene.input.KeyEvent

interface GlobalKeyboardListener {
    var isShiftPressed : Boolean

    var onGlobalKeyPress: ((event: KeyEvent) -> Unit)?
}