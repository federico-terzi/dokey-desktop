package app.control_panel.devices_tab

import app.control_panel.ControlPanelTab
import app.control_panel.devices_tab.device_list.DeviceList
import javafx.scene.input.KeyEvent
import system.image.ImageResolver
import system.server.HandshakeDataBuilder
import java.util.*
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.nio.file.FileSystems
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import system.BroadcastManager
import system.server.MobileServer
import utils.SystemInfoManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

const val QRCODE_UPDATE_INTERVAL = 1000L

class DevicesTab(val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                 val handshakeDataBuilder: HandshakeDataBuilder) : ControlPanelTab() {

    private val qrImageView = ImageView()
    private val titleLabel = Label(resourceBundle.getString("connect_dokey_app"))
    private val descriptionLabel = Label(resourceBundle.getString("connect_dokey_app_desc"))
    private val deviceInfoLabel = Label(resourceBundle.getString("connected_devices"))
    private val deviceList = DeviceList(imageResolver)

    private var qrCodeUpdateTimer = Timer()
    private var oldQRPayload : String? = null

    init {
        qrImageView.fitWidth = 300.0
        qrImageView.fitHeight = 300.0

        titleLabel.styleClass.add("device-tab-title-label")
        descriptionLabel.styleClass.add("device-tab-description-label")
        descriptionLabel.isWrapText = true

        val hBox = HBox()
        hBox.alignment = Pos.CENTER_LEFT
        hBox.styleClass.add("device-tab-connected-box")
        // TODO: change image
        val imageView = ImageView()
        imageView.fitWidth = 20.0
        imageView.fitHeight = 20.0
        imageResolver.loadInto("asset:airplay", 20, imageView)

        deviceInfoLabel.alignment = Pos.TOP_LEFT
        hBox.children.addAll(imageView, deviceInfoLabel)

        alignment = Pos.TOP_CENTER
        children.addAll(qrImageView, titleLabel, descriptionLabel, hBox, deviceList)
    }

    private fun requestQRCode(onQrReady: (Image?) -> Unit, onQrCreationError: () -> Unit) {
        Thread {
            val payload = handshakeDataBuilder.getHandshakePayload()
            // Make sure the payload is valid
            if (payload == null) {
                Platform.runLater { onQrCreationError() }
                return@Thread
            }

            // Check if the payload has changed and should be recreated
            if (payload == oldQRPayload) {
                Platform.runLater {
                    onQrReady(null)
                }
            }else{
                val stream = generateQR(payload, 600, 600)
                val image = Image(stream, 600.0, 600.0, true, true)
                Platform.runLater {
                    oldQRPayload = payload
                    onQrReady(image)
                }
            }
        }.start()
    }

    override fun onFocus() {
        // Setup and start the qr code update timer that continuously updates the QR with the current net configuration
        qrCodeUpdateTimer = Timer()
        oldQRPayload = null
        qrCodeUpdateTimer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                Platform.runLater {
                    requestQRCode(
                            onQrReady = {image ->
                                if (image != null) {
                                    qrImageView.image = image
                                }
                            },
                            onQrCreationError = {
                                qrImageView.image = null
                                // TODO: show an error message instead of the qr code
                            })
                }
            }
        }, 0, QRCODE_UPDATE_INTERVAL)

        // Register the device list
        deviceList.items = FXCollections.observableArrayList(MobileServer.connectedDevices)

        // Register the broadcast listeners for app wide events
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.DEVICE_CONNECTED, connectedDeviceListChangedEvent)
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.DEVICE_DISCONNECTED, connectedDeviceListChangedEvent)
    }

    override fun onUnfocus() {
        // Unregister the QR code timer
        qrCodeUpdateTimer.cancel()

        // Unregister the broadcasts
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.DEVICE_CONNECTED, connectedDeviceListChangedEvent)
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.DEVICE_DISCONNECTED, connectedDeviceListChangedEvent)
    }

    private val connectedDeviceListChangedEvent = BroadcastManager.BroadcastListener {
        Platform.runLater {
            deviceList.items = FXCollections.observableArrayList(MobileServer.connectedDevices)
        }
    }

    companion object {
        fun generateQR(text: String, height: Int, width: Int) : InputStream {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)

            val bos = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos)

            return ByteArrayInputStream(bos.toByteArray())
        }
    }
}