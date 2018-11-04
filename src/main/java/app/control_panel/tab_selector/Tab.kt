package app.control_panel.tab_selector

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.util.Duration
import system.image.ImageResolver

const val UNSELECTED_TAB_VERTICAL_OFFSET = 10.0

class Tab(val imageResolver: ImageResolver, val tabLabel : String, val tabImage : String) : Button() {
    private val nameLabel : Label
    private val imageView : ImageView

    private var _selected = false
    var selected : Boolean
        get() = _selected
        set(value) {
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

                if (_selected != value) {
                    animateSelectionOut()
                }else{
                    nameLabel.opacity = 0.0
                    imageView.translateY = UNSELECTED_TAB_VERTICAL_OFFSET
                }
            }

            _selected = value
        }

    init {
        styleClass.add("tab-selector-tab")

        // Needed to make the button hitbox equals to the shape
        isPickOnBounds = false

        val vBox = VBox()
        vBox.alignment = Pos.CENTER
        nameLabel = Label(tabLabel)
        nameLabel.opacity = 0.0
        nameLabel.isCache = false

        imageView = ImageView()
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0
        imageView.translateY = UNSELECTED_TAB_VERTICAL_OFFSET
        imageResolver.loadInto(tabImage, 24, imageView)
        vBox.children.addAll(imageView, nameLabel)


        isCache = true
        cacheHint = CacheHint.SPEED

        prefWidth = TAB_WIDTH

        graphic = vBox
    }

    private fun animateSelectionIn() {
        isCache = false

        val fadeInTransition = FadeTransition(Duration(200.0), nameLabel)
        fadeInTransition.fromValue = 0.0
        fadeInTransition.toValue = 1.0

        val translateTransition = TranslateTransition(Duration(200.0), imageView)
        translateTransition.interpolator = Interpolator.EASE_BOTH
        translateTransition.toY = 0.0

        val parallelTransition = ParallelTransition(fadeInTransition, translateTransition)
        parallelTransition.setOnFinished { isCache = true }
        parallelTransition.play()
    }

    private fun animateSelectionOut() {
        val fadeInTransition = FadeTransition(Duration(200.0), nameLabel)
        fadeInTransition.fromValue = 1.0
        fadeInTransition.toValue = 0.0

        val translateTransition = TranslateTransition(Duration(200.0), imageView)
        translateTransition.interpolator = Interpolator.EASE_BOTH
        translateTransition.toY = UNSELECTED_TAB_VERTICAL_OFFSET

        val parallelTransition = ParallelTransition(fadeInTransition, translateTransition)
        parallelTransition.play()
    }
}