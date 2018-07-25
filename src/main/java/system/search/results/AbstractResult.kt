package system.search.results

import javafx.scene.image.Image
import system.context.SearchContext

abstract class AbstractResult(val context: SearchContext) : Result {
    // Should be true if the result image is an icon, and should change color when selected
    override var hasIconImage : Boolean = true

    // If all results of a category have the same icon, this will be set.
    override val staticImage : Image? = null

    // If the image is different for each result, it has to be dynamically requested
    override fun requestImage(callback: (image: Image, imageHash: String?) -> Unit) {}  // Default NOP
    // Hash of the image for caching purposes, if null it means that it is not cachable
    override val imageHash : String? = null
}