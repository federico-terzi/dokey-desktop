package app.tray_icon

import javafx.stage.Screen
import java.awt.image.BufferedImage
import java.util.*

class MacTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    override val loadingIcon: BufferedImage
    override val defaultIcon: BufferedImage

    init {
        loadingIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/mac/loading.png")
        defaultIcon = AbstractTrayIconManager.loadBufferedImage("/assets/tray/mac/icon.png")

        // Stimate the initial icon position
        val bounds = Screen.getPrimary().bounds
        _iconX = bounds.width.toInt() - 280
        _iconY = 20
    }

}