package app.tray_icon

import app.MainApp
import javafx.stage.Screen
import java.awt.Graphics2D
import java.awt.Image
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.*
import java.util.logging.Logger
import javax.imageio.ImageIO

class MSTrayIconManager(resourceBundle: ResourceBundle) : AbstractTrayIconManager(resourceBundle) {
    val loadingIcon: BufferedImage = loadBufferedImage("/assets/tray/win/wait.png")
    val defaultIcon: BufferedImage = loadBufferedImage("/assets/tray/win/icon.png")

    init {
        // Stimate the first icon position
        val bounds = Screen.getPrimary().bounds
        _iconX = bounds.width.toInt() - 250
        _iconY = bounds.height.toInt() - 20
    }

    private var trayIcon: TrayIcon? = null
    private var trayIconSize : Int = 0

    override fun initialize() {
        java.awt.Toolkit.getDefaultToolkit()

        if (!java.awt.SystemTray.isSupported()) {
            LOG.warning("No system tray support, application exiting.")
            System.exit(0)
        }

        // Get the system tray bar
        val systemTray = java.awt.SystemTray.getSystemTray()

        // Setup the tray icon
        trayIcon = TrayIcon(loadingIcon)
        trayIconSize = trayIcon!!.size.width

        systemTray.add(trayIcon)

        // Setup the click listener
        trayIcon?.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                when (e!!.button) {
                    1 -> {  // LEFT CLICK
                        // Update the icon position
                        _iconX = e!!.x
                        _iconY = e!!.y

                        onTrayIconClicked?.invoke()
                    }
                    3 -> {  // RIGHT CLICK

                    }
                }
            }
        })

        // Start the loading mechanism
        loading = true
    }

    override fun updateStatusTooltip(text: String) {
        trayIcon?.toolTip = text
    }

    override fun updateStatusIcon() {
        if (loading) {
            trayIcon?.image = loadingIcon.getScaledInstance(trayIconSize, -1, Image.SCALE_SMOOTH)
        }else{
            trayIcon?.image = defaultIcon.getScaledInstance(trayIconSize, -1, Image.SCALE_SMOOTH)
        }
    }

    companion object {
        val LOG = Logger.getGlobal()

        fun loadBufferedImage(resourcePath: String): BufferedImage {
            val iconStream = AbstractTrayIconManager::class.java.getResourceAsStream(resourcePath)
            val image = ImageIO.read(iconStream)
            iconStream.close()
            return image
        }
    }

}