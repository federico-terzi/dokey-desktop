package app.tray_icon

import app.bindings.JavaMacNativeUI
import javafx.stage.Screen
import system.BroadcastManager
import system.ResourceUtils
import java.awt.image.BufferedImage
import java.util.*

class MacTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    private val callback = JavaMacNativeUI.StatusItemClickCallback { x, y ->
        _iconX = x
        _iconY = y

        onTrayIconClicked?.invoke()
    }

    private val loadingIconPath = ResourceUtils.getResource("/assets/tray/mac/wait.png").absolutePath
    private val defaultIconPath = ResourceUtils.getResource("/assets/tray/mac/icon.png").absolutePath

    init {
        // Stimate the initial icon position
        val bounds = Screen.getPrimary().bounds
        _iconX = bounds.width.toInt() - 280
        _iconY = 20
    }

    override fun initialize() {
        JavaMacNativeUI.INSTANCE.initializeStatusItem()
        JavaMacNativeUI.INSTANCE.setStatusItemImage(loadingIconPath)
        JavaMacNativeUI.INSTANCE.setStatusItemAction(callback)

        // Register broadcast receivers for control panel events
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.CONTROL_PANEL_OPENED_EVENT, controlPanelOpenedEventReceiver)
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.CONTROL_PANEL_CLOSED_EVENT, controlPanelClosedEventReceiver)
    }

    override fun updateStatusTooltip(text: String) {
        JavaMacNativeUI.INSTANCE.setStatusItemTooltip(text)
    }

    override fun updateStatusIcon() {
        if (loading) {
            JavaMacNativeUI.INSTANCE.setStatusItemImage(loadingIconPath)
        }else{
            JavaMacNativeUI.INSTANCE.setStatusItemImage(defaultIconPath)
        }
    }

    /*
    Broadcast managers used to highlight the status bar when the control panel is opened
     */

    private val controlPanelOpenedEventReceiver = BroadcastManager.BroadcastListener {
        JavaMacNativeUI.INSTANCE.setStatusItemHighlighted(1)
    }

    private val controlPanelClosedEventReceiver = BroadcastManager.BroadcastListener {
        JavaMacNativeUI.INSTANCE.setStatusItemHighlighted(0)
    }
}