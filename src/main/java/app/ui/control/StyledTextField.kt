package app.ui.control

import javafx.scene.control.TextField

class StyledTextField : TextField() {
    init {
        styleClass.add("styled-text-field")
    }
}