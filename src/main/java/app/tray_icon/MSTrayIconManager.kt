package app.tray_icon

import java.awt.image.BufferedImage
import java.util.*

class MSTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    override val loadingIcon: BufferedImage
    override val defaultIcon: BufferedImage

    init {
        loadingIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/win/loading.png")
        defaultIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/win/icon.png")
    }

}