package system.search.results

import javafx.scene.image.Image

interface Result {
    // Should be true if the result image is an icon, and should change color when selected
    var hasIconImage : Boolean

    val title : String
    val description : String?

    fun executeAction()

    // If all results of a category have the same icon, this will be set.
    val staticImage : Image?

    // If the image is different for each result, it has to be dynamically requested
    fun requestImage(callback: (image: Image, imageHash: String?) -> Unit)
    // Hash of the image for caching purposes, if null it means that it is not cachable
    val imageHash : String?
}