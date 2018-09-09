package app.control_panel.command_tab

import app.ui.control.ExpandableSearchBar
import app.ui.control.IconButton
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import system.image.ImageResolver

class Toolbar(val imageResolver: ImageResolver) : HBox() {
    private val searchBar = ExpandableSearchBar(imageResolver)
    private val filterLabel = Label()
    private val filterButton = IconButton(imageResolver, "asset:filter", 18)

    init {
        styleClass.add("command-tab-toolbar")

        val spacerPane = Pane()
        HBox.setHgrow(spacerPane, Priority.ALWAYS)

        children.addAll(searchBar, spacerPane, filterLabel, filterButton)
    }
}