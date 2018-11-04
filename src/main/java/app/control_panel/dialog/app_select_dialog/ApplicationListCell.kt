package app.control_panel.dialog.app_select_dialog

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.applications.Application
import system.image.ImageResolver

class ApplicationListCell(val imageResolver: ImageResolver) : ListCell<Application>() {
    private val imageView : ImageView = ImageView()
    private val nameLabel : Label = Label()
    private val pathLabel : Label = Label()
    private val hBox = HBox()

    init {
        styleClass.add("application-list-cell")

        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        nameLabel.styleClass.add("application-list-cell-title")
        pathLabel.styleClass.add("application-list-cell-path")

        val vBox = VBox()
        vBox.styleClass.add("application-list-cell-vbox")
        vBox.children.addAll(nameLabel, pathLabel)
        vBox.minWidth = 0.0
        vBox.prefWidth = 1.0
        HBox.setHgrow(vBox, Priority.ALWAYS)

        hBox.children.addAll(imageView, vBox)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minWidth = 0.0
        hBox.prefWidth = 1.0
        graphic = hBox
    }

    private fun addContent(entry: Application) {
        // Setup the labels
        nameLabel.text = entry.name
        pathLabel.text = entry.id  // TODO: put something more user friendly here than the path of the application or store id

        // Get the image for the application
        imageResolver.loadInto("app:${entry.id}", 48, imageView)

        graphic = hBox
    }

    override fun updateItem(result: Application?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}