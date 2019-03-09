package system.external.photoshop

enum class PhotoshopSliderAction(val actionId: String, val actionLabel: String, val defaultMin: Float, val defaultMax: Float) {
    LAYER_OPACITY("opacity", "Change Layer Opacity", 0f, 100f),
    BRUSH_SIZE("brushsize", "Change Brush Size", 1f, 500f);

    override fun toString(): String {
        return actionLabel
    }

    companion object {
        fun find(id: String?) : PhotoshopSliderAction? {
            return values().find { it.actionId == id }
        }
    }
}