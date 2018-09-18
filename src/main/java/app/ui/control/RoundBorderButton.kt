package app.ui.control

import javafx.scene.control.Button

class RoundBorderButton(text : String) : Button(text) {
    init {
        styleClass.add("round-border-button")
    }
}