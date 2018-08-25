package app.search.sectionlistview

import app.search.sectionlistview.adapters.ResultViewAdapter
import app.search.sectionlistview.adapters.SeparatorViewAdapter
import app.search.sectionlistview.adapters.ViewAdapter
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import system.image.ImageResolver
import system.search.results.Result

/**
 * Represents a general list view element in the SectionListView, capable of
 * holding a result or a separator
 */
data class ListViewEntry(val category: String?, val result: Result?) {
    val isSeparator : Boolean
        get() = category != null

    val isResult : Boolean
        get() = result != null

    /**
     * Return the correct view adapter based on the type of entry
     */
    fun getViewAdapter(imageResolver: ImageResolver, fallback: Image) : ViewAdapter? {
        if (isSeparator) {
            return SeparatorViewAdapter()
        }else if (isResult) {
            return ResultViewAdapter(imageResolver, fallback)
        }

        return null
    }

    /**
     * Return the preferred height for the current list cell based on the
     * entry type.
     */
    fun getHeight() : Double? {
        if (isSeparator) {
            return 40.0
        }else if (isResult) {
            return 55.0
        }

        return null
    }
}