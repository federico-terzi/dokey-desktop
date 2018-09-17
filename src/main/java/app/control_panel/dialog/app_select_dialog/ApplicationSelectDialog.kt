package app.control_panel.dialog.app_select_dialog

import app.control_panel.ControlPanelStage
import app.ui.control.CollapseExpandButton
import app.ui.control.ExpandableSearchBar
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.applications.Application
import system.applications.ApplicationManager
import system.image.ImageResolver

val COLLAPSED_APP_LIST_VIEW_HEIGHT = 300.0
val EXPANDED_APP_LIST_VIEW_HEIGHT = 450.0

class ApplicationSelectDialog(parent: BlurrableStage, imageResolver: ImageResolver,
                              val applicationManager: ApplicationManager)
    : OverlayDialog(parent, imageResolver) {

    var onApplicationSelected: ((Application) -> Unit)? = null

    private val searchBar = ExpandableSearchBar(imageResolver)
    private val applicationListView = ApplicationListView(imageResolver)
    private val showAllAppsBtn = CollapseExpandButton(imageResolver, "Show all apps", "Show active apps")  // TODO: i18n
    private val contentBox = VBox()

    private val applications = FXCollections.observableArrayList<Application>()

    private var showAllApps = false

    private var searchQuery: String? = null

    init {
        applicationListView.minHeight = COLLAPSED_APP_LIST_VIEW_HEIGHT
        applicationListView.prefHeight = 1.0
        VBox.setVgrow(applicationListView, Priority.ALWAYS)

        showAllAppsBtn.styleClass.add("show-all-apps-btn")
        VBox.setVgrow(showAllAppsBtn, Priority.NEVER)
        contentBox.alignment = Pos.CENTER

        contentBox.children.addAll(applicationListView, showAllAppsBtn)

        initializeUI()

        applicationListView.items = applications

        searchBar.onSearchChanged = {
            searchQuery = it
            loadApplications()
        }

        showAllAppsBtn.onExpand = {
            showAllApps = true
            loadApplications()

            applicationListView.minHeight = EXPANDED_APP_LIST_VIEW_HEIGHT
            adaptHeight()
        }
        showAllAppsBtn.onCollapse = {
            showAllApps = false
            loadApplications()

            applicationListView.minHeight = COLLAPSED_APP_LIST_VIEW_HEIGHT
            adaptHeight()

        }

        // List view selection listener
        applicationListView.setOnMouseClicked {
            if (it.clickCount == 2) {
                userSelectedApplication()
            }
        }
        applicationListView.setOnKeyPressed {
            if (it.code == KeyCode.ENTER) {
                userSelectedApplication()
            }
        }

        Platform.runLater { loadApplications() }
    }

    fun loadApplications() {
        var startingApps = if (showAllApps) {
            applicationManager.applicationList
        } else {
            applicationManager.activeApplications
        }

        if (searchQuery != null) {
            startingApps = startingApps.filter { it.name.contains(searchQuery!!, true) }
        }

        applications.setAll(startingApps)

        applications.sortWith(Comparator { o1, o2 -> o1.name.compareTo(o2.name) })

        // Select the first element
        if (applicationListView.items.size > 0) {
            applicationListView.selectionModel.select(0)
        }
    }

    fun userSelectedApplication() {
        val selectedApplication = applicationListView.selectionModel.selectedItem
        if (selectedApplication != null) {
            onApplicationSelected?.invoke(selectedApplication)
            onClose()
        }
    }

    override fun defineTopSectionComponent(): Node? {
        return searchBar
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}