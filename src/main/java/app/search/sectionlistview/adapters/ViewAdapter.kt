package app.search.sectionlistview.adapters

import app.search.sectionlistview.ListViewEntry
import javafx.scene.layout.HBox

/**
 * A ViewAdapter is used to create ( and when possible update ) the
 * view associated with a list cell.
 */
interface ViewAdapter {
    /**
     * Check if the current instance of the view adapter is compatible
     * with the given entry. If it is, the "updateView" method can be
     * called to update the view elements without initializing new
     * components and thus saving memory.
     */
    fun isCompatible(entry: ListViewEntry) : Boolean

    /**
     * Update the elements of the current view to adapt to the given
     * entry. This avoid instantiating new instances when possible and
     * thus saving memory.
     */
    fun updateView(entry: ListViewEntry)

    /**
     * Get the base box of the view adapter.
     */
    fun getView() : HBox
}