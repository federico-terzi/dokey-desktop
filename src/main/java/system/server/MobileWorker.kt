package system.server

import java.net.Socket
import net.model.DeviceInfo
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import system.bookmarks.BookmarkImportAgent.LOG
import net.DEManager




class MobileWorker(val socket: Socket, val key: ByteArray) : Thread(), ApplicationContextAware {
    var connectedDevice : DeviceInfo? = null
    var isConnected = false

    var deviceConnectionListener: OnDeviceConnectionListener? = null

    private var context : ApplicationContext? = null

    @Volatile var shouldStop = false

    private var service : MobileService? = null

    init {
        name = "Mobile Worker"
    }

    var onConnected : (() -> Unit)? = null
    var onDisconnected : (() -> Unit)? = null

    override fun run() {
        try {
            // Create the engine service
            service = context!!.getBean(MobileService::class.java, socket, key, object : DEManager.OnConnectionListener {
                override fun onInvalidKey() {
                    LOG.warning("A device tried to connect to the computer with the wrong key.")
                    shouldStop = true

                    deviceConnectionListener?.onInvalidKeyConnectionAttempt()
                }

                override fun onConnectionNotAccepted(deviceInfo: DeviceInfo, version: Int) {
                    connectedDevice = deviceInfo
                    LOG.warning("Connection not accepted by the phone: " + deviceInfo.name + " with version: " + version)
                    shouldStop = true

                    // Send the notification
                    deviceConnectionListener?.onDesktopVersionTooLow(deviceInfo)
                }

                override fun onReceiverVersionTooLow(deviceInfo: DeviceInfo, version: Int) {
                    connectedDevice = deviceInfo
                    LOG.warning("Not accepting connection, phone has version too low: " + deviceInfo.name + " with version: " + version)
                    shouldStop = true

                    // Send the notification
                    deviceConnectionListener?.onMobileVersionTooLow(deviceInfo)
                }

                override fun onConnectionStarted(deviceInfo: DeviceInfo, version: Int) {
                    connectedDevice = deviceInfo
                    LOG.info("Connection accepted by: " + deviceInfo.name + " with version: " + version)
                    // Send the connect notification
                    onConnected?.invoke()
                    deviceConnectionListener?.onDeviceConnected(deviceInfo)

                    isConnected = true
                }
            })
            service?.initialize()

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
        onDisconnected?.invoke()
        deviceConnectionListener?.onDeviceDisconnected(connectedDevice)
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