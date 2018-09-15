package app.ui.control

import javafx.scene.control.ComboBox

open class StyledComboBox<T> : ComboBox<T>() {
    init {
        styleClass.add("styled-combo-box")
    }
}