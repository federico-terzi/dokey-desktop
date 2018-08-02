package app.control_panel

import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox

abstract class ControlPanelTab : VBox() {
    abstract fun onFocus()
    abstract fun onGlobalKeyPress(event: KeyEvent)
}