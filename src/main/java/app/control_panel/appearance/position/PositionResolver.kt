package app.control_panel.appearance.position

import app.tray_icon.TrayIconManager
import javafx.stage.Stage

abstract class PositionResolver(val trayIconManager: TrayIconManager) {
    abstract fun positionStageOnScreen(stage: Stage)
}