package app.control_panel.layout_editor_tab

import app.ui.control.IconButton
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import model.section.ApplicationSection
import model.section.LaunchpadSection
import model.section.Section
import model.section.SystemSection
import system.applications.ApplicationManager
import system.image.ImageResolver

class LayoutToolbar(val imageResolver: ImageResolver, val applicationManager: ApplicationManager) : HBox() {
    val applicationLabel = Label()

    var onNewLayoutRequested : (() -> Unit)? = null

    init {
        alignment = Pos.CENTER
        styleClass.add("layout-toolbar")

        val spacingPanel = Pane()
        HBox.setHgrow(spacingPanel, Priority.ALWAYS)

        val newLayoutBtn = IconButton(imageResolver,"asset:file-plus", 24)
        val moreBtn = IconButton(imageResolver, "asset:more-vertical", 24)

        children.addAll(applicationLabel, spacingPanel, newLayoutBtn, moreBtn)
    }

    fun setCurrentSection(section: Section) {
        applicationLabel.text = section.name
    }
}