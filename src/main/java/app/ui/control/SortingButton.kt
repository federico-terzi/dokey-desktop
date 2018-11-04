package app.ui.control

import app.ui.model.Sorting
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import system.image.ImageResolver

/**
 * Used in a list view header, as in the command tab.
 */
class SortingButton(val imageResolver: ImageResolver, val title : String) : Button() {
    private val imageView : ImageView

    var sorting : Sorting = Sorting.ASCENDING

    private var _sortingEnabled = false
    var sortingEnabled
        get() = _sortingEnabled
        set(value) {
            _sortingEnabled = value
            render()
        }

    var onSortingSelected : ((Sorting) -> Unit)? = null

    init {
        styleClass.add("sorting-button")

        text = title
        imageView = ImageView()
        imageView.fitWidth = 12.0
        imageView.fitHeight = 12.0
        imageView.opacity = 0.0
        imageResolver.loadInto("asset:sort", 12, imageView)

        graphic = imageView
        contentDisplay = ContentDisplay.RIGHT

        setOnMouseClicked {
            // Invert the sorting
            sorting = if (sorting == Sorting.ASCENDING) Sorting.DESCENDING else Sorting.ASCENDING

            if (!sortingEnabled) {
                sortingEnabled = true
            }else{
                render()
            }

            onSortingSelected?.invoke(sorting)
        }
    }

    fun render() {
        if (sortingEnabled) {
            imageView.opacity = 1.0
            when (sorting) {
                Sorting.ASCENDING -> {
                    imageView.rotate = 180.0
                }
                Sorting.DESCENDING -> {
                    imageView.rotate = 0.0
                }
            }
        }else{
            imageView.opacity = 0.0
        }
    }
}