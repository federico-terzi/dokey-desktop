package system.search.results

import javafx.scene.image.Image
import system.context.SearchContext

abstract class AbstractResult(val context: SearchContext) : Result {
    // Should be true if the result image is an icon, and should change color when selected
    override var hasIconImage : Boolean = true

    override val imageId: String?
        get() = null
}