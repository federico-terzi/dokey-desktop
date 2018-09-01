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

/**
 * This component extends ListView to implement the Category label feature used
 * to distinguish between results
 */
class SectionListView(val preferredWidth: Double, val imageResolver: ImageResolver,
                      val onResultSelected : () -> Unit) : ListView<ListViewEntry>() {
    private val results : ObservableList<ListViewEntry> = FXCollections.observableArrayList()

    init {
        styleClass.add("search-list-view")

        // Setup the list cells
        val fallback = ImageResolver.getImage("/assets/photo.png", 32)
        cellFactory = Callback<ListView<ListViewEntry>, ListCell<ListViewEntry>> {
            ListViewCell(preferredWidth, fallback, imageResolver) as ListCell<ListViewEntry>
        }

        // Result list view click listener, execute the corresponding action
        setOnMouseClicked { event ->
            // Make sure the clicked item is a result
            if (results[selectionModel.selectedIndex].isResult) {
                executeCurrentResult()
            }else{
                selectionModel.clearSelection()
            }
        }

        items = results
    }

    /**
     * Populate the list view with the given results.
     */
    fun setResults(results: SortedMap<ResultCategory, MutableList<Result>>) {
        // Convert the given results to the observable list
        val finalResults = mutableListOf<ListViewEntry>()
        results.forEach {
            finalResults.add(ListViewEntry(it.key.name, null))
            it.value.forEach {
                finalResults.add(ListViewEntry(null, it))
            }
        }
        this.results.setAll(finalResults)
    }

    /**
     * Adapt the height of the list view to the exact height that the cells occupies.
     */
    fun adaptHeight() {
        prefHeight = getComputedHeight()
    }

    /**
     * Select the result corresponding to the given index, automatically
     * ignoring the Separators items
     */
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

    /**
     * Select the first element of the next section.
     */
    fun selectNextSection() {
        val currentCategory = getCurrentSection()

        for (i in selectionModel.selectedIndex until (results.size - 1)) {
            if (results[i].isSeparator && results[i].category != currentCategory) {
                selectionModel.select(i+1)
                return
            }
        }

        selectIndex(0)
    }

    /**
     * Get the index of the currently selected result, automatically
     * correcting the count ignoring the separator items.
     */
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

    /**
     * Get the section of the currently selected item.
     */
    fun getCurrentSection() : String? {
        val currentlySelected = selectionModel.selectedIndex
        for (i in currentlySelected downTo 0) {
            if (results[i].isSeparator) {
                return results[i].category
            }
        }

        return null
    }

    /**
     * Get the category of the currently selected item.
     */
    fun getCurrentCategory() : ResultCategory? {
        val currentlySelected = selectionModel.selectedItem as ListViewEntry
        if (currentlySelected != null && currentlySelected.isResult) {
            return currentlySelected.result?.category
        }

        return null
    }

    /**
     * Get the currently selected result.
     */
    fun getSelectedResult() : Result? {
        if (selectionModel.selectedIndex < 0) {
            return null
        }

        return results[selectionModel.selectedIndex]?.result
    }

    /**
     * Execute the currently selected result
     */
    fun executeCurrentResult() {
        val currentResult = getSelectedResult()
        Thread {
            currentResult?.executeAction()
        }.start()
    }

    /**
     * Get the total number of results in the list view, ignoring
     * the separator items.
     */
    fun getTotalItems() : Int {
        return results.filter { it.isResult }.count()
    }

    /**
     * Calculate the height of the listview based on the sum of the individual
     * list cells
     */
    private fun getComputedHeight() : Double {
        return results.sumByDouble { it.getHeight()!! }
    }
}