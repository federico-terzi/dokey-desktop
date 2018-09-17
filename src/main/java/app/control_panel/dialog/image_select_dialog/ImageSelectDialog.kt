package app.control_panel.dialog.image_select_dialog

import app.ui.control.ExpandableSearchBar
import app.ui.control.grid_view.GridView
import app.ui.control.grid_view.model.GridViewEntry
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.image.ImageResolver

class ImageSelectDialog(parent: BlurrableStage, imageResolver: ImageResolver)
    : OverlayDialog(parent, imageResolver) {

    private val searchBar = ExpandableSearchBar(imageResolver)
    private val imageGridView = GridView(4, imageResolver)
    private val contentBox = VBox()

    private var searchQuery: String? = null

    init {
        imageGridView.minHeight = 400.0
        imageGridView.prefHeight = 1.0
        VBox.setVgrow(imageGridView, Priority.ALWAYS)


        contentBox.alignment = Pos.CENTER
        contentBox.children.addAll(imageGridView)

        initializeUI()

        searchBar.onSearchChanged = {
            searchQuery = it
            loadImages()
        }

        Platform.runLater { loadImages() }
    }

    fun loadImages() {
        val images = imageResolver.list("static")
        imageGridView.entries = images!!.map { GridViewEntry(it.name, it.id) }
    }

    override fun defineTopSectionComponent(): Node? {
        return searchBar
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}