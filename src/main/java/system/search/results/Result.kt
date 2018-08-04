package system.search.results

import javafx.scene.image.Image

interface Result {
    // Should be true if the result image is an icon, and should change color when selected
    var hasIconImage : Boolean

    val title : String
    val description : String?
    val extra : String?

    fun executeAction()

    val imageId : String?

    fun generateDragAndDropPayload() : String?
}