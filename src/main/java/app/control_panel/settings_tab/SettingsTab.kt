package app.control_panel.settings_tab

import app.control_panel.ControlPanelTab
import app.ui.control.ToggleButton
import javafx.scene.input.KeyEvent
import system.image.ImageResolver

class SettingsTab(val imageResolver: ImageResolver) : ControlPanelTab() {
    init {
        val button = ToggleButton(imageResolver)
        children.add(button)
    }

    override fun onFocus() {

    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }

}