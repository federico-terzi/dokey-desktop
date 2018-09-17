package app.control_panel.command_tab.list

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import system.image.ImageResolver
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CommandListCell(val imageResolver: ImageResolver) : ListCell<Command>() {
    private val imageView : ImageView = ImageView()
    private val nameLabel : Label = Label()
    private val descriptionLabel : Label = Label()
    private val dateLabel : Label = Label()
    private val hBox = HBox()

    private val dayFormatter = DateTimeFormatter.ofPattern("MMM, dd")
    private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        styleClass.add("command-list-cell")

        // Setup the image as the default one
        imageView.image = imageResolver.resolveImage("asset:image", 48)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        nameLabel.styleClass.add("command-list-cell-title")
        descriptionLabel.styleClass.add("command-list-cell-desc")
        dateLabel.styleClass.add("command-list-cell-date")

        val vBox = VBox()
        vBox.styleClass.add("command-list-cell-vbox")
        vBox.children.addAll(nameLabel, descriptionLabel)
        vBox.minWidth = 0.0
        vBox.prefWidth = 1.0
        HBox.setHgrow(vBox, Priority.ALWAYS)

        HBox.setHgrow(dateLabel, Priority.SOMETIMES)

        hBox.children.addAll(imageView, vBox, dateLabel)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minWidth = 0.0
        hBox.prefWidth = 1.0
        graphic = hBox
    }

    private fun addContent(entry: Command) {
        // Setup the labels
        nameLabel.text = entry.title
        descriptionLabel.text = entry.description ?: ""

        // Setup the date label
        val editDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.lastEdit!!), TimeZone.getDefault().toZoneId())
        // If the edit date is less than a day, show the hour and minute
        // day and month otherwise.
        if (System.currentTimeMillis() - entry.lastEdit!! < 86_400_000) {
            dateLabel.text = hourFormatter.format(editDate)
        }else{
            dateLabel.text = dayFormatter.format(editDate)
        }

        // Get the image for the command
        val iconId = entry.iconId ?: "asset:image"
        imageResolver.resolveImageAsync(iconId, 48) {image, externalThread ->
            if (image != null) {
                if (externalThread) {
                    Platform.runLater {
                        imageView.image = image
                    }
                }else{
                    imageView.image = image
                }
            }
        }

        graphic = hBox
    }

    override fun updateItem(result: Command?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}