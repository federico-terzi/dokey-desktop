package system.search.results

import javafx.scene.image.Image
import system.context.SearchContext
import system.drag_and_drop.DNDCommandProcessor

abstract class AbstractResult(val context: SearchContext) : Result {
    // Should be true if the result image is an icon, and should change color when selected
    override var hasIconImage : Boolean = true

    override val imageId: String? = null

    override fun generateDragAndDropPayload(): String? {
        val internalPayload = generateDragAndDropPayloadInternal()
        if (internalPayload == null) {
            return null
        }else{
            return "${DNDCommandProcessor.dragAndDropPrefix}:$internalPayload"
        }
    }

    open fun generateDragAndDropPayloadInternal() : String? = null

    override fun executeAction() {}
}