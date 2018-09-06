package app.control_panel.layout_editor_tab

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

        val newLayoutBtn = Button()
        val newLayoutImage = imageResolver.resolveImage("asset:file-plus", 24)
        val newLayoutImageView = ImageView(newLayoutImage)
        newLayoutImageView.fitHeight = 24.0
        newLayoutImageView.fitWidth = 24.0
        newLayoutBtn.graphic = newLayoutImageView

        val moreBtn = Button()
        val moreImage = imageResolver.resolveImage("asset:more-vertical", 24)
        val moreImageView = ImageView(moreImage)
        moreImageView.fitHeight = 24.0
        moreImageView.fitWidth = 24.0
        moreBtn.graphic = moreImageView

        children.addAll(applicationLabel, spacingPanel, newLayoutBtn, moreBtn)
    }

    fun setCurrentSection(section: Section) {
        applicationLabel.text = section.name
    }
}