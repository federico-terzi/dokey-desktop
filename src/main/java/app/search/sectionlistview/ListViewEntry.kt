package app.search.sectionlistview

import app.search.sectionlistview.adapters.ResultViewAdapter
import app.search.sectionlistview.adapters.SeparatorViewAdapter
import app.search.sectionlistview.adapters.ViewAdapter
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import system.image.ImageResolver
import system.search.results.Result

data class ListViewEntry(val category: String?, val result: Result?) {
    val isSeparator : Boolean
        get() = category != null

    val isResult : Boolean
        get() = result != null

    fun getViewAdapter(imageResolver: ImageResolver, fallback: Image) : ViewAdapter? {
        if (isSeparator) {
            return SeparatorViewAdapter()
        }else if (isResult) {
            return ResultViewAdapter(imageResolver, fallback)
        }

        return null
    }
}