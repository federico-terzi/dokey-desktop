package app.ui.control.grid_view

import app.ui.control.grid_view.model.GridViewEntry
import app.ui.control.grid_view.model.GridViewRow
import javafx.scene.control.ListCell
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import system.image.ImageResolver

class GridViewListCell(val colNumber: Int, val imageResolver: ImageResolver, val onSelected: (GridViewEntry) -> Unit) : ListCell<GridViewRow>() {
    private val gridBox = GridPane()
    private val buttons : List<GridViewButton>

    init {
        styleClass.add("grid-view-list-cell")

        // Setup the grid constraints
        (0 until colNumber).forEach { index ->
            val cc = ColumnConstraints()
            cc.hgrow = Priority.ALWAYS // allow column to grow
            cc.isFillWidth = true // ask nodes to fill space for column
            cc.percentWidth = 100.0
            gridBox.columnConstraints.add(cc)
        }

        // Create the buttons
        buttons = (0 until colNumber).map { GridViewButton(imageResolver) }

        // Add all the buttons
        buttons.forEachIndexed { index, button ->
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            GridPane.setFillWidth(button, true)
            gridBox.add(button, index, 0)
        }

        gridBox.minWidth = 0.0
        gridBox.prefWidth = 1.0
    }

    private fun addContent(current: GridViewRow) {
        // Setup the valid buttons
        current.items.forEachIndexed { index, entry ->
            buttons[index].entry = entry

            buttons[index].setOnAction {
                onSelected(entry)
            }
        }

        // Empty the remaining ones if the number of items is less than colNumber
        (current.items.size until colNumber).forEach { index ->
            buttons[index].entry = null
            buttons[index].onAction = null
        }

        graphic = gridBox
    }

    override fun updateItem(result: GridViewRow?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}