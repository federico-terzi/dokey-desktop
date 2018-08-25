package app.search.sectionlistview

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import system.image.ImageResolver
import system.search.results.Result
import system.search.results.ResultCategory
import java.util.*

class SectionListView(val preferredWidth: Double, val imageResolver: ImageResolver) : ListView<ListViewEntry>() {
    private val results : ObservableList<ListViewEntry> = FXCollections.observableArrayList()

    init {
        // Setup the list cells
        val fallback = ImageResolver.getImage("/assets/photo.png", 32)
        cellFactory = Callback<ListView<ListViewEntry>, ListCell<ListViewEntry>> {
            ListViewCell(preferredWidth, fallback, imageResolver) as ListCell<ListViewEntry>
        }

        items = results
    }

    fun setResults(results: SortedMap<ResultCategory, MutableList<Result>>) {
        // Convert the given results to the observable list
        val finalResults = mutableListOf<ListViewEntry>()
        results.forEach {
            finalResults.add(ListViewEntry(it.key.name.toUpperCase(), null))
            it.value.forEach {
                finalResults.add(ListViewEntry(null, it))
            }
        }
        this.results.setAll(finalResults)
    }

    fun getComputedHeight() : Double {
        return results.size * 55.0  // TODO: cambiare l'altezza
    }

    fun adaptHeight() {
        prefHeight = getComputedHeight()
    }

    fun selectIndex(index: Int) {
        var count = 0
        for (i in 0 until results.size) {
            if (results[i].isSeparator) {
                continue
            }

            if (count == index) {
                selectionModel.select(i)
            }

            count++
        }
    }

    fun selectNextCategory() {
        val currentCategory = getCurrentCategory()

        for (i in selectionModel.selectedIndex until (results.size - 1)) {
            if (results[i].isSeparator && results[i].category != currentCategory) {
                selectionModel.select(i+1)
                return
            }
        }

        selectIndex(0)
    }

    fun getSelectedIndex() : Int {
        val realSelectedIndex = selectionModel.selectedIndex
        var count = 0
        for (i in 0 until realSelectedIndex) {
            if (results[i].isResult) {
                count++
            }
        }

        return count
    }

    fun getCurrentCategory() : String? {
        val currentlySelected = selectionModel.selectedIndex
        for (i in currentlySelected downTo 0) {
            if (results[i].isSeparator) {
                return results[i].category
            }
        }

        return null
    }

    fun getSelectedResult() : Result? {
        return results[selectionModel.selectedIndex]?.result
    }

    fun getTotalItems() : Int {
        return results.filter { it.isResult }.count()
    }
}