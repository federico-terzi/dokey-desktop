package app.ui.control

import app.ui.animation.WidthTransition
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.util.Duration
import system.image.ImageResolver

const val COLLAPSED_WIDTH = 70.0
const val EXPANDED_WIDTH = 150.0

class ExpandableSearchBar(val imageResolver: ImageResolver) : HBox() {
    private val imageView : ImageView
    private val textField = TextField()

    var onSearchChanged : ((String) -> Unit)? = null

    init {
        styleClass.add("expandable-search-bar")

        alignment = Pos.CENTER_LEFT

        imageView = ImageView()
        imageView.fitHeight = 18.0
        imageView.fitWidth = 18.0
        imageView.styleClass.add("expandable-search-bar-text-field")
        imageResolver.loadInto("asset:search", 18, imageView)

        textField.promptText = "Search..."  // TODO: make international
        textField.prefWidth = 70.0

        textField.focusedProperty().addListener { _, _, focused ->
            if (focused) {
                expand()
            }else{
                // Contract only if empty
                if (textField.text.isBlank()) {
                    contract()
                }
            }
        }

        textField.textProperty().addListener { _, _, value ->
            onSearchChanged?.invoke(value)
        }

        prefWidth = Region.USE_COMPUTED_SIZE

        children.addAll(imageView, textField)
    }

    fun expand() {
        val transition = WidthTransition(textField, Duration(200.0), EXPANDED_WIDTH)
        transition.play()
    }

    fun contract() {
        val transition = WidthTransition(textField, Duration(200.0), COLLAPSED_WIDTH)
        transition.play()
    }
}