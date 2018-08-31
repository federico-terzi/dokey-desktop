package app.control_panel.tab_selector

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.util.Duration
import system.image.ImageResolver

class Tab(val imageResolver: ImageResolver, val tabLabel : String, val tabImage : String) : Button() {
    private val nameLabel : Label
    private val imageView : ImageView

    private var _selected = false
    var selected : Boolean
        get() = _selected
        set(value) {
            println(value)
            if (value) {
                styleClass.add("tab-selected")

                if (_selected != value) {
                    animateSelectionIn()
                }else{
                    nameLabel.opacity = 1.0
                }
            }else{
                styleClass.clear()
                styleClass.add("tab-selector-tab")

                nameLabel.opacity = 0.0
                imageView.translateY = 10.0
            }

            _selected = value
        }

    init {
        styleClass.add("tab-selector-tab")

        val vBox = VBox()
        vBox.alignment = Pos.CENTER
        nameLabel = Label(tabLabel)
        val image = imageResolver.resolveImage(tabImage, 24)
        imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        vBox.children.addAll(imageView, nameLabel)

        graphic = vBox
    }

    private fun animateSelectionIn() {
        val fadeInTransition = FadeTransition(Duration(200.0), nameLabel)
        fadeInTransition.fromValue = 0.0
        fadeInTransition.toValue = 1.0

        val translateTransition = TranslateTransition(Duration(200.0), imageView)
        translateTransition.interpolator = Interpolator.EASE_BOTH
        translateTransition.toY = 0.0

        val parallelTransition = ParallelTransition(fadeInTransition, translateTransition)
        parallelTransition.play()
    }
}