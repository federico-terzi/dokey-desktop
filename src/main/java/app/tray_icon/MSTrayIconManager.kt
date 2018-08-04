package app.tray_icon

import javafx.stage.Screen
import java.awt.image.BufferedImage
import java.util.*

class MSTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    override val loadingIcon: BufferedImage
    override val defaultIcon: BufferedImage

    init {
        loadingIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/win/loading.png")
        defaultIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/win/icon.png")

        // Stimate the first icon position
        val bounds = Screen.getPrimary().bounds
        _iconX = bounds.width.toInt() - 250
        _iconY = bounds.height.toInt() - 20
    }

}