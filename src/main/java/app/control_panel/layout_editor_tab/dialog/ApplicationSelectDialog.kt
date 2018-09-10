package app.control_panel.layout_editor_tab.dialog

import app.control_panel.ControlPanelStage
import app.ui.control.ExpandableSearchBar
import app.ui.dialog.OverlayDialog
import javafx.scene.Node
import javafx.scene.layout.VBox
import system.applications.ApplicationManager
import system.image.ImageResolver

class ApplicationSelectDialog(controlPanelStage: ControlPanelStage, imageResolver: ImageResolver,
                              val applicationManager: ApplicationManager)
    : OverlayDialog(controlPanelStage, imageResolver) {

    private val searchBar = ExpandableSearchBar(imageResolver)

    init {
        initializeUI()
    }

    override fun defineTopSectionComponent(): Node? {
        return searchBar
    }

    override fun defineContentBoxComponent(): VBox? {
        return super.defineContentBoxComponent()
    }
}