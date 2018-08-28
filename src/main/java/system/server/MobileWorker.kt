package system.server

import java.net.Socket
import net.model.DeviceInfo
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import system.bookmarks.BookmarkImportAgent.LOG
import net.DEDaemon
import net.DEManager




class MobileWorker(val socket: Socket, val key: ByteArray) : Thread(), ApplicationContextAware {
    var receiverInfo : DeviceInfo? = null

    var deviceConnectionListener: OnDeviceConnectionListener? = null

    private var context : ApplicationContext? = null

    @Volatile private var shouldStop = false

    private var service : MobileService? = null

    init {
        name = "Mobile Worker"
    }

    override fun run() {
        try {
            // Create the engine service
            service = context!!.getBean(MobileService::class.java, socket, object : DEManager.OnConnectionListener {
                override fun onInvalidKey() {
                    LOG.warning("A device tried to connect to the computer with the wrong key.")
                    shouldStop = true

                    deviceConnectionListener?.onInvalidKeyConnectionAttempt()
                }

                override fun onConnectionNotAccepted(deviceInfo: DeviceInfo, version: Int) {
                    receiverInfo = deviceInfo
                    LOG.warning("Connection not accepted by the phone: " + deviceInfo.name + " with version: " + version)
                    shouldStop = true

                    // Send the notification
                    deviceConnectionListener?.onDesktopVersionTooLow(deviceInfo)
                }

                override fun onReceiverVersionTooLow(deviceInfo: DeviceInfo, version: Int) {
                    receiverInfo = deviceInfo
                    LOG.warning("Not accepting connection, phone has version too low: " + deviceInfo.name + " with version: " + version)
                    shouldStop = true

                    // Send the notification
                    deviceConnectionListener?.onMobileVersionTooLow(deviceInfo)
                }

                override fun onConnectionStarted(deviceInfo: DeviceInfo, version: Int) {
                    receiverInfo = deviceInfo
                    LOG.info("Connection accepted by: " + deviceInfo.name + " with version: " + version)
                    // Send the connect notification
                    deviceConnectionListener?.onDeviceConnected(deviceInfo)
                }
            })

            // Set up the connection closed listener
            service?.onConnectionClosed = {shouldStop = true}

            // Start the daemons
            service?.start()

            // Loop until should terminate is true
            while (!shouldStop) {
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


        LOG.fine("Closing EngineWorker: $name")

        // Close the service
        service?.close()

        // Send the disconnect notification
        deviceConnectionListener?.onDeviceDisconnected(receiverInfo)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        context = applicationContext
    }

    /**
     * Used to notify when a device connects or disconnects from the server
     */
    interface OnDeviceConnectionListener {
        fun onDeviceConnected(deviceInfo: DeviceInfo?)
        fun onDeviceDisconnected(deviceInfo: DeviceInfo?)
        fun onDesktopVersionTooLow(deviceInfo: DeviceInfo?)
        fun onMobileVersionTooLow(deviceInfo: DeviceInfo?)
        fun onInvalidKeyConnectionAttempt()
    }
}