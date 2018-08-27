package app.tray_icon

import app.MainApp
import java.awt.Graphics2D
import java.awt.Image
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.*
import java.util.logging.Logger
import javax.imageio.ImageIO

abstract class AbstractTrayIconManager(val resourceBundle: ResourceBundle) : TrayIconManager {
    override var onTrayIconClicked: (() -> Unit)? = null

    abstract val loadingIcon: BufferedImage
    abstract val defaultIcon: BufferedImage

    private var _statusText = resourceBundle.getString("initializing")
    override var statusText: String
        get() = _statusText
        set(value) {
            _statusText = value
            updateTooltipText()
        }


    private var _loading: Boolean = false
    override var loading: Boolean
        get() = _loading
        set(value) {
            if (value) {
                rotationTimer.schedule(object : TimerTask() {
                    override fun run() {
                        javax.swing.SwingUtilities.invokeLater(Runnable {
                            if (value) {
                                currentRotationAngle += ROTATION_DEGREE
                                updateIcon(rotateImage(loadingIcon, currentRotationAngle))

                            }
                        })
                    }
                },
                        0L,
                        ROTATION_INTERVAL
                )
            } else {
                currentRotationAngle = 0
                rotationTimer.cancel()

                // Change the icon to ready with a delay
                Thread {
                    Thread.sleep(500)
                    javax.swing.SwingUtilities.invokeLater(Runnable {
                        updateIcon(defaultIcon)
                    })
                }.start()
            }
            _loading = value
        }

    private var currentRotationAngle = 0
    private var rotationTimer = Timer()

    private var trayIcon: TrayIcon? = null
    private var trayIconSize : Int = 0

    protected var _iconX : Int = 0
    protected var _iconY : Int = 0

    override val iconX: Int
        get() = _iconX

    override val iconY: Int
        get() = _iconY

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
                // Update the icon position
                _iconX = e!!.x
                _iconY = e!!.y

                onTrayIconClicked?.invoke()
            }
        })

        // Start the loading mechanism
        loading = true
    }

    private fun updateTooltipText() {
        trayIcon?.toolTip = "Dokey ${MainApp.DOKEY_VERSION}\n$statusText"
    }

    private fun updateIcon(icon: BufferedImage) {
        trayIcon?.image = icon.getScaledInstance(trayIconSize, -1, Image.SCALE_SMOOTH)
    }

    companion object {
        const val ROTATION_INTERVAL = 500L
        const val ROTATION_DEGREE = 45

        val LOG = Logger.getGlobal()

        fun loadBufferedImage(resourcePath: String): BufferedImage {
            val iconStream = AbstractTrayIconManager::class.java.getResourceAsStream(resourcePath)
            val image = ImageIO.read(iconStream)
            iconStream.close()
            return image
        }

        fun rotateImage(image: BufferedImage, angle: Int): BufferedImage {
            val imgNew = BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR)
            val g = imgNew.graphics as Graphics2D
            g.rotate(Math.toRadians(angle.toDouble()), (image.getWidth() / 2).toDouble(), (image.getHeight() / 2).toDouble())
            g.drawImage(image, 0, 0, null)
            return imgNew
        }
    }
}