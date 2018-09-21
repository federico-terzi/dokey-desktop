package app.control_panel

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox

class ComingSoonTab : ControlPanelTab() {

    init {
        styleClass.add("coming-soon-tab")

        val label = Label("Coming soon!")
        label.maxWidth = Double.MAX_VALUE
        label.alignment = Pos.CENTER
        children.add(label)
    }

    override fun onFocus() {
    }

    override fun onGlobalKeyPress(event: KeyEvent) {
    }

}