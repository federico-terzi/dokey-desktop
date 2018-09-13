package app.ui.control

import javafx.scene.control.Button

class RoundedButton(text : String) : Button(text) {
    init {
        styleClass.add("rounded-button")
    }
}