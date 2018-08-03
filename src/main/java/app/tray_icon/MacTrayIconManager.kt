package app.tray_icon

import java.awt.image.BufferedImage
import java.util.*

class MacTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    override val loadingIcon: BufferedImage
    override val defaultIcon: BufferedImage

    init {
        loadingIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/mac/loading.png")
        defaultIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/mac/icon.png")

        // Stimate the initial icon position
        // TODO
    }

}