package app.ui.popup

import javafx.scene.Node
import javafx.scene.layout.VBox
import javafx.stage.Popup
import javafx.stage.Window

open class StyledPopup : Popup() {
    val contentBox = VBox()

    init {
        contentBox.styleClass.add("styled-popup-box")

        content.add(contentBox)

        isAutoHide = true
    }

    fun showUnderMouse(owner: Window, mouseX: Double, mouseY: Double) {
        show(owner)

        x = mouseX - width/2
        y = mouseY
    }
}