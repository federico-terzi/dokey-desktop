package system.external.photoshop

enum class PhotoshopCommandAction(val actionId: String, val actionLabel: String) {
    NEXT_LAYER("nextlayer", "Next Layer"),
    PREV_LAYER("prevlayer", "Previous Layer");

    override fun toString(): String {
        return actionLabel
    }

    companion object {
        fun find(id: String?) : PhotoshopCommandAction? {
            return values().find { it.actionId == id }
        }
    }
}