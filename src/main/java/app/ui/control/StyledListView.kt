package app.ui.control

import javafx.scene.control.ListView

open class StyledListView<T> : ListView<T>() {
    init {
        styleClass.add("styled-list-view")
    }
}