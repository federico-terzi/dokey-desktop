package app.ui.control

import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Duration
import system.image.ImageResolver

const val SELECTED_OFFSET = 18.0

class ToggleButton(imageResolver: ImageResolver) : HBox() {
    private val ball = Circle(0.0,0.0, 5.0)

    private var _checked = false

    var checked : Boolean
        get() = _checked
        set(value) {
            if (value) {
                select()
            }else{
                unselect()
            }

            _checked = value
        }

    var onToggle : ((selected: Boolean) -> Unit)? = null

    init {
        styleClass.add("toggle-button")
        ball.styleClass.add("toggle-button-ball")

        alignment = Pos.CENTER_LEFT

        children.add(ball)

        setOnMouseClicked {
            checked = !checked  // Toggle
            onToggle?.invoke(checked)
        }
    }

    private fun select() {
        ball.fill = Color.BLACK

        val transition = TranslateTransition(Duration(100.0), ball)
        transition.toX = SELECTED_OFFSET
        transition.play()
    }

    private fun unselect() {
        ball.fill = Color.WHITE

        val transition = TranslateTransition(Duration(100.0), ball)
        transition.toX = 0.0
        transition.play()
    }
}