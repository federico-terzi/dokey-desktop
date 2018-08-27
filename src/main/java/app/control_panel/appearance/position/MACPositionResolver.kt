package app.control_panel.appearance.position

import app.tray_icon.TrayIconManager
import javafx.stage.Stage

class MACPositionResolver(trayIconManager: TrayIconManager) : PositionResolver(trayIconManager) {
    override fun positionStageOnScreen(stage: Stage) {
        stage.x = trayIconManager.iconX - stage.width/2
        stage.y = trayIconManager.iconY + 0.0
    }

}