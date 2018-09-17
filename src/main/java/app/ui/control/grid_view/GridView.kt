package app.ui.control.grid_view

import app.ui.control.grid_view.model.GridViewEntry
import app.ui.control.grid_view.model.GridViewRow
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import system.image.ImageResolver

class GridView(val colNumber: Int, val imageResolver: ImageResolver) : ListView<GridViewRow>() {
    private val rows = FXCollections.observableArrayList<GridViewRow>()

    private var _entries = listOf<GridViewEntry>()
    var entries : List<GridViewEntry>
        get() = _entries
        set(value) {
            rows.setAll(generateRowsFromEntries(colNumber, value))
            _entries = value
        }

    var onEntrySelected : ((GridViewEntry) -> Unit)? = null

    init {
        styleClass.add("grid-view")

        setCellFactory {
            GridViewListCell(colNumber, imageResolver) {
                onEntrySelected?.invoke(it)
            }
        }

        items = rows
    }

    companion object {
        /**
         * Organize the list of items into a grid
         */
        fun generateRowsFromEntries(colNumber: Int, entries : List<GridViewEntry>) : List<GridViewRow> {
            val output = mutableListOf<GridViewRow>()

            var currentColumn = 0
            var currentItems = mutableListOf<GridViewEntry>()

            entries.forEach {entry ->
                currentItems.add(entry)

                currentColumn++

                if (currentColumn == colNumber) {
                    currentColumn = 0
                    output.add(GridViewRow(currentItems))

                    currentItems = mutableListOf()
                }
            }

            // If the row is not completed, add it manually
            if (currentColumn != 0) {
                output.add(GridViewRow(currentItems))
            }

            return output
        }
    }
}