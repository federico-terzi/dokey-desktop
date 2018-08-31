package app.control_panel.layout_editor.bar.selectors

import javafx.animation.FadeTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.util.Duration
import model.section.Section

abstract class Selector(val context: SelectorContext, val section: Section, val priority : Int) : Comparable<Selector>, Button() {
    abstract val imageId : String

    private var _selected = false
    var selected : Boolean
        get() = _selected
        set(value) {
            if (value != _selected) {
                isCache = false

                if (value) {
                    selectorNode.opacity = 1.0

                    val transition = TranslateTransition(Duration(300.0), selectorNode)
                    transition.fromY = 10.0
                    transition.toY = 1.0
                    transition.setOnFinished { isCache = true }
                    transition.play()
                }else{
                    val transition = TranslateTransition(Duration(300.0), selectorNode)
                    transition.fromY = 1.0
                    transition.toY = 10.0
                    transition.setOnFinished { selectorNode.opacity = 0.0; isCache = true }
                    transition.play()
                }
            }

            _selected = value
        }

    private val selectorNode : Rectangle = Rectangle()

    fun initialize() {
        val image = context.imageResolver.resolveImage(imageId, 32)
        val imageView = ImageView(image)
        imageView.fitHeight = 32.0
        imageView.fitWidth = 32.0

//        selectorNode.content = "M25 48 L42 48 L33 40 Z"
        selectorNode.width = 27.0
        selectorNode.height = 4.0
        selectorNode.arcHeight = 5.0
        selectorNode.arcWidth = 5.0
        selectorNode.fill = Color.WHITE
        selectorNode.opacity = 0.0

        val vBox = VBox()
        vBox.styleClass.add("app-selector-vbox")
        vBox.alignment = Pos.CENTER
        vBox.children.addAll(imageView, selectorNode)
        setGraphic(vBox)

        isCache = true
        cacheHint = CacheHint.SPEED
    }

    override fun compareTo(other: Selector): Int {
        return priority.compareTo(other.priority)
    }
}