package app.ui.popup

import app.ui.stage.BlurrableStage
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Control
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

    fun showForComponent(parent: BlurrableStage, component: Control) {
        show(parent)

        // Calculate the component coordinated by traversing the node hierarchy
        var finalX = 0.0
        var finalY = 0.0
        var current: Parent = component
        while (current.parent != null) {
            finalX += current.layoutX
            finalY += current.layoutY
            current = current.parent
        }

        finalX += parent.x
        finalY += parent.y

        finalX += component.width / 2 - width/2
        finalY += component.height / 2

        x = finalX + 5
        y = finalY
    }
}