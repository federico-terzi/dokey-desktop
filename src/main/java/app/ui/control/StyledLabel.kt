package app.ui.control

import javafx.scene.control.TextField

class StyledLabel : TextField() {
    init {
        styleClass.add("styled-label")
    }
}