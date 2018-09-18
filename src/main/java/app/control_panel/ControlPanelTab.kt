package app.control_panel

import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox

abstract class ControlPanelTab : VBox() {
    open fun onFocus() {}
    open fun onUnfocus() {}
    open fun onGlobalKeyPress(event: KeyEvent) {}
}