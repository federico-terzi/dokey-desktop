package system.external.photoshop

enum class PhotoshopSliderAction(val actionId: String, val actionLabel: String) {
    LAYER_OPACITY("opacity", "Change Layer Opacity");

    override fun toString(): String {
        return actionLabel
    }

    companion object {
        fun find(id: String?) : PhotoshopSliderAction? {
            return values().find { it.actionId == id }
        }
    }
}