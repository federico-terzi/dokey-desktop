package app.control_panel.devices_tab.device_list

import javafx.scene.control.ListView
import net.model.DeviceInfo
import system.image.ImageResolver

class DeviceList(val imageResolver: ImageResolver) : ListView<DeviceInfo>() {
    init {
        // Setup the list cells
        setCellFactory { DeviceListCell(imageResolver) }

        styleClass.add("device-list-view")
    }
}