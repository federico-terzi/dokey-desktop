package app.control_panel.appearance.position

import app.tray_icon.TrayIconManager
import javafx.stage.Stage

class MSPositionResolver(trayIconManager: TrayIconManager) : PositionResolver(trayIconManager) {
    override fun positionStageOnScreen(stage: Stage) {
        stage.x = trayIconManager.iconX - stage.width/2
        stage.y = trayIconManager.iconY - stage.height - 10
    }

}