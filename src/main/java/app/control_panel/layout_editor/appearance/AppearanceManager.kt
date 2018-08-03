package app.control_panel.layout_editor.appearance

import app.tray_icon.TrayIconManager
import javafx.stage.Stage

abstract class AppearanceManager(val trayIconManager: TrayIconManager) {
    abstract fun positionStageOnScreen(stage: Stage)
//    abstract fun loadSpecificStyle(stage)
}