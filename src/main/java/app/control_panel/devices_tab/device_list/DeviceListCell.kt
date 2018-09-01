package app.control_panel.devices_tab.device_list

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import net.model.DeviceInfo
import system.image.ImageResolver

/**
 * The list cell used in the Device List View
 */
class DeviceListCell(val imageResolver: ImageResolver) : ListCell<DeviceInfo>() {
    private val imageView : ImageView
    private val nameLabel : Label
    private val hBox = HBox()
    private val disconnectBtn : Button

    init {
        styleClass.add("device-list-cell")

        val image = imageResolver.resolveImage("asset:smartphone", 24)
        imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        nameLabel = Label()
        val pane = Pane()
        HBox.setHgrow(pane, Priority.ALWAYS)

        disconnectBtn = Button()
        val disconnectImageView = ImageView(imageResolver.resolveImage("asset:x-circle", 20))
        disconnectImageView.fitWidth = 20.0
        disconnectImageView.fitHeight = 20.0
        disconnectBtn.graphic = disconnectImageView

        hBox.children.addAll(imageView, nameLabel, pane, disconnectBtn)
        hBox.alignment = Pos.CENTER_LEFT
        graphic = hBox
    }

    private fun addContent(entry: DeviceInfo) {
        nameLabel.text = entry.name

        graphic = hBox
    }

    override fun updateItem(result: DeviceInfo?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            addContent(result!!)
        }
    }
}