package app.control_panel.layout_editor

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
        val newLayoutImage = imageResolver.resolveImage("asset:file-plus", 20)
        val newLayoutImageView = ImageView(newLayoutImage)
        newLayoutImageView.fitHeight = 20.0
        newLayoutImageView.fitWidth = 20.0
        newLayoutBtn.graphic = newLayoutImageView

        val moreBtn = Button()
        val moreImage = imageResolver.resolveImage("asset:more-vertical", 20)
        val moreImageView = ImageView(moreImage)
        moreImageView.fitHeight = 20.0
        moreImageView.fitWidth = 20.0
        moreBtn.graphic = moreImageView

        children.addAll(applicationLabel, spacingPanel, newLayoutBtn, moreBtn)
    }

    fun setCurrentSection(section: Section) {
        applicationLabel.text = getSectionLabel(section)
    }

    private fun getSectionLabel(section: Section): String? {
        return when(section) {
            is LaunchpadSection -> "Launchpad"
            is SystemSection -> "System"
            is ApplicationSection -> applicationManager.getApplication(section.appId).name
            else -> "Undefined"
        }
    }
}