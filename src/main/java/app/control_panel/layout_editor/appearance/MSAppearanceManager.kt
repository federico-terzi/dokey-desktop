package app.control_panel.layout_editor.appearance

import app.tray_icon.TrayIconManager
import javafx.stage.Stage

class MSAppearanceManager(trayIconManager: TrayIconManager) : AppearanceManager(trayIconManager) {
    override fun positionStageOnScreen(stage: Stage) {
        stage.x = 300.0
        stage.y = 300.0
        println("${stage.width}")
        // TODO attenzione che la dimensione viene calcolata dopo il primo show
    }

}