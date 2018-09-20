package app.control_panel

import app.alert.AlertFactory
import app.alert.model.AlertOption
import javafx.application.Platform
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
        val option = AlertOption("Provolone") {
            this@ComingSoonTab.opacity = 0.1
        }
        AlertFactory.instance.custom("Ciao", "Ciaone", listOf(option)).show()
    }

    override fun onGlobalKeyPress(event: KeyEvent) {
    }

}