package app.control_panel.devices_tab

import app.control_panel.ControlPanelTab
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
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class DevicesTab(val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                 val handshakeDataBuilder: HandshakeDataBuilder) : ControlPanelTab() {

    private val qrImageView = ImageView()
    private val titleLabel = Label(resourceBundle.getString("connect_dokey_app"))
    private val descriptionLabel = Label(resourceBundle.getString("connect_dokey_app_desc"))

    init {
        qrImageView.fitWidth = 300.0
        qrImageView.fitHeight = 300.0

        titleLabel.styleClass.add("device-tab-title-label")
        descriptionLabel.styleClass.add("device-tab-description-label")
        descriptionLabel.isWrapText = true

        alignment = Pos.TOP_CENTER
        children.addAll(qrImageView, titleLabel, descriptionLabel)
    }

    override fun onFocus() {
        requestQRCode() {image ->
            qrImageView.image = image
        }
    }

    private fun requestQRCode(onQrReady: (Image) -> Unit) {
        Thread {
            val payload = handshakeDataBuilder.getHandshakePayload()
            val stream = generateQR(payload, 600, 600)
            val image = Image(stream, 600.0, 600.0, true, true)
            Platform.runLater {
                onQrReady(image)
            }
        }.start()
    }

    override fun onGlobalKeyPress(event: KeyEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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