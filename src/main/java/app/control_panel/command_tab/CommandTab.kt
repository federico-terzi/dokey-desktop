package app.control_panel.command_tab

import app.control_panel.ControlPanelTab
import javafx.scene.input.KeyEvent
import system.image.ImageResolver
import java.util.*

class CommandTab(val imageResolver: ImageResolver, val resourceBundle: ResourceBundle) : ControlPanelTab() {

    private val toolbar = Toolbar(imageResolver)

    init {
        children.addAll(toolbar)
    }

    override fun onFocus() {
    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }
}