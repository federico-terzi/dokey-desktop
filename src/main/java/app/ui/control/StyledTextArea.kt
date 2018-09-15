package app.ui.control

import javafx.scene.control.TextArea

class StyledTextArea : TextArea() {
    init {
        styleClass.add("styled-text-area")

        minWidth = 0.0
        prefWidth = 1.0
    }
}