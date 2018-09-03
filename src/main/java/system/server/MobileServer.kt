package system.server

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.util.logging.Logger

/**
 * This number is sent directly when a new connection is created and makes possible for the receiver to
 * check if the connection is from a dokey server.
 */
const val DOKEY_NUMBER : Int = 5623981

class MobileServer(val serverSocket: ServerSocket, val key : ByteArray) : Thread(), ApplicationContextAware {
    @Volatile private var shouldStop = false

    private val LOG = Logger.getGlobal()

    private var context : ApplicationContext? = null

    var deviceConnectionListener : MobileWorker.OnDeviceConnectionListener? = null

    init {
        name = "Mobile Server"
    }

    override fun run() {
        LOG.fine("Server started!")

        while(!shouldStop) {
            try {
                val socket = serverSocket.accept()

                // Send the dokey number
                val dos = DataOutputStream(socket.getOutputStream())
                dos.write(DOKEY_NUMBER)

                // Create the worker and start it
                val worker = context!!.getBean(MobileWorker::class.java, socket, key)
                worker.deviceConnectionListener = deviceConnectionListener
                worker.start()

                LOG.info("Connected with: " + socket.inetAddress.toString());
            }catch (e: IOException) {
                LOG.severe("Socket error: $e")
            }
        }

        try {
            serverSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun stopServer() {
        shouldStop = true
    }

    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        context = applicationContext
    }
}