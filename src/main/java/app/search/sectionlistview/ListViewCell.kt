package app.search.sectionlistview

import app.search.sectionlistview.adapters.ViewAdapter
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import system.image.ImageResolver
import system.search.results.Result

class ListViewCell(private val listWidth: Double, private val fallback: Image,
                   private val imageResolver: ImageResolver) : ListCell<ListViewEntry>() {
    private var viewAdapter: ViewAdapter? = null

    init {
        prefHeight = 55.0 // TODO: change
    }

    private fun addContent(entry: ListViewEntry) {
        if (viewAdapter == null || !viewAdapter!!.isCompatible(entry)) {
            viewAdapter = entry.getViewAdapter(imageResolver, fallback)
        }

        viewAdapter!!.updateView(entry)
        val view = viewAdapter!!.getView()
        view.alignment = Pos.CENTER_LEFT
        view.maxWidth = listWidth - 50

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