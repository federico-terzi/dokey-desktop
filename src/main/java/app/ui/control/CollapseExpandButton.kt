package app.ui.control

import javafx.animation.RotateTransition
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import javafx.util.Duration
import system.image.ImageResolver

class CollapseExpandButton(val imageResolver: ImageResolver,
                           val collapsedText : String, val expandedText: String) : Button() {
    private val imageView : ImageView

    var onExpand : (() -> Unit)? = null
    var onCollapse : (() -> Unit)? = null

    var collapsed = true

    init {
        styleClass.add("collapse-expand-button")

        val image = imageResolver.resolveImage("asset:expand", 20)
        imageView = ImageView(image)
        imageView.fitHeight = 20.0
        imageView.fitWidth = 20.0

        graphic = imageView
        text = collapsedText

        contentDisplay = ContentDisplay.BOTTOM

        setOnAction {
            if (collapsed) {
                text = expandedText
                rotateUp()
                onExpand?.invoke()
            }else{
                text = collapsedText
                rotateDown()
                onCollapse?.invoke()
            }

            collapsed = !collapsed
        }
    }

    fun rotateUp() {
        val transition = RotateTransition(Duration(200.0), imageView)
        transition.fromAngle = 0.0
        transition.toAngle = 180.0
        transition.play()
    }

    fun rotateDown() {
        val transition = RotateTransition(Duration(200.0), imageView)
        transition.fromAngle = 180.0
        transition.toAngle = 0.0
        transition.play()
    }
}