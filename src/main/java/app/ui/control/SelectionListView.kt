package app.ui.control

import javafx.scene.control.ListView

open class SelectionListView<T> : ListView<T>() {
    init {
        styleClass.add("selection-list-view")
    }
}