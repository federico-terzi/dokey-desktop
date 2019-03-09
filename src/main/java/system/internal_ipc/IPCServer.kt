package system.internal_ipc

import system.BroadcastManager
import java.io.DataInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.charset.Charset
import java.util.logging.Logger

class IPCServer : Thread() {
    @Volatile private var shouldStop = false

    private val LOG = Logger.getGlobal()

    init {
        name = "IPCServer"
    }

    override fun run() {
        val serverSocket = ServerSocket(0, 5, InetAddress.getLoopbackAddress())
        IPCManager.registerPort(serverSocket.localPort)

        LOG.info("IPCServer started on port: ${serverSocket.localPort}")

        while(!shouldStop) {
            val socket = serverSocket.accept()
            val din = DataInputStream(socket.getInputStream())

            // Read the request type
            val requestType = din.readInt()

            // Read body len
            val bodyLen = din.readInt()
            val body : String? = if (bodyLen > 0) {
                val bodyBytes = ByteArray(bodyLen)
                din.readFully(bodyBytes, 0, bodyLen)
                String(bodyBytes, Charset.forName("UTF-8"))
            }else{
                null
            }

            LOG.info("Received IPC request type ${requestType} with body ${body}")

            executeRequest(requestType, body)

            socket.close()
        }
    }

    val requestHandlers = mutableMapOf<Int, (String?) -> Unit>(
            1 to { body ->
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_CONTROL_PANEL_REQUEST_EVENT, null)
            }
    )

    fun executeRequest(type: Int, body: String?) {
        if (!requestHandlers.containsKey(type)) {
            LOG.warning("Invalid IPC request type: ${type}")
        }

        requestHandlers[type]?.invoke(body)
    }

    fun stopServer() {
        shouldStop = true
    }
}