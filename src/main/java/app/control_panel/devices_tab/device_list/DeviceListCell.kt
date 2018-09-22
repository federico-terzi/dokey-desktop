package app.control_panel.devices_tab.device_list

import app.ui.control.IconButton
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import net.model.DeviceInfo
import system.BroadcastManager
import system.image.ImageResolver

/**
 * The list cell used in the Device List View
 */
class DeviceListCell(val imageResolver: ImageResolver) : ListCell<DeviceInfo>() {
    private val imageView : ImageView
    private val nameLabel : Label
    private val hBox = HBox()
    private val disconnectBtn : IconButton

    init {
        styleClass.add("device-list-cell")

        val image = imageResolver.resolveImage("asset:smartphone", 24)
        imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        nameLabel = Label()
        val pane = Pane()
        HBox.setHgrow(pane, Priority.ALWAYS)

        disconnectBtn = IconButton(imageResolver, "asset:x-circle", 20)

        hBox.children.addAll(imageView, nameLabel, pane, disconnectBtn)
        hBox.alignment = Pos.CENTER_LEFT
        graphic = hBox
    }

    private fun addContent(entry: DeviceInfo) {
        nameLabel.text = entry.name

        disconnectBtn.setOnAction {
            BroadcastManager.getInstance().sendBroadcast(BroadcastManager.REQUEST_DEVICE_DISCONNECT, entry.id)
        }

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