package app.search.sectionlistview.adapters

import app.search.sectionlistview.ListViewEntry
import javafx.scene.layout.HBox

interface ViewAdapter {
    fun isCompatible(entry: ListViewEntry) : Boolean
    fun updateView(entry: ListViewEntry)
    fun getView() : HBox
}