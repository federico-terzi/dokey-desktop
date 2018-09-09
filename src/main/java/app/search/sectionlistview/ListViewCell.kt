package app.search.sectionlistview

import app.search.sectionlistview.adapters.ViewAdapter
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import system.image.ImageResolver
import system.search.results.Result

/**
 * The list cell used in the SectionListView.
 */
class ListViewCell(private val fallback: Image,
                   private val imageResolver: ImageResolver) : ListCell<ListViewEntry>() {
    // The current view adapter, kept in memory make updates more efficient
    private var viewAdapter: ViewAdapter? = null

    private fun addContent(entry: ListViewEntry) {
        /*
        If the current view adapter is compatible with the given entry,
        update it directly.
        If the adapter is not compatible, create a new one.
        This mechanism makes updating list view elements more efficient
        because usually the item is of the same type of the previous.
         */
        if (viewAdapter == null || !viewAdapter!!.isCompatible(entry)) {
            viewAdapter = entry.getViewAdapter(imageResolver, fallback)

            prefHeight = entry.getHeight()!!
        }

        viewAdapter!!.updateView(entry)
        val view = viewAdapter!!.getView()
        view.alignment = Pos.CENTER_LEFT
        view.minWidth = 0.0
        view.prefWidth = 1.0

        graphic = view
    }

    override fun updateItem(result: ListViewEntry?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}