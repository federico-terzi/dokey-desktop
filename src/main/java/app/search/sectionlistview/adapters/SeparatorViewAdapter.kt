package app.search.sectionlistview.adapters

import app.search.sectionlistview.ListViewEntry
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class SeparatorViewAdapter : ViewAdapter {
    private val hBox = HBox()
    private val title = Label()

    init {
        hBox.styleClass.add("dokey-search-separator-box")
        title.styleClass.add("dokey-search-separator-title")

        hBox.children.add(title)
    }

    private fun populateFields(separatorText: String) {
        title.text = separatorText
    }

    override fun isCompatible(entry: ListViewEntry): Boolean {
        return entry.isSeparator
    }

    override fun updateView(entry: ListViewEntry) {
        if (entry.category != null) {
            populateFields(entry.category)
        }else{
            hBox.children.clear()
        }
    }

    override fun getView(): HBox {
        return hBox
    }
}