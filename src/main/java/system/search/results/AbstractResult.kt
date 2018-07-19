package system.search.results

import javafx.scene.image.Image
import system.search.SearchContext

abstract class AbstractResult(val context: SearchContext) {
    // Should be true if the result image is an icon, and should change color when selected
    var hasIconImage : Boolean = true

    abstract val title : String
    abstract val description : String?

    abstract fun executeAction()

    // If all results of a category have the same icon, this will be set.
    open val staticImage : Image? = null

    // If the image is different for each result, it has to be dynamically requested
    open fun requestImage(callback: (image: Image, imageHash: String?) -> Unit) {}  // Default NOP
    // Hash of the image for caching purposes, if null it means that it is not cachable
    open val imageHash : String? = null
}