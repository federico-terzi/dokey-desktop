package system.internal_ipc

import system.BroadcastManager
import system.storage.StorageManager
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.Socket
import java.util.logging.Logger
import java.nio.charset.Charset

const val IPC_PORT_FILENAME : String = "ipcport.txt"

const val IPC_OPEN_COMMAND = 1

object IPCManager {
    private val LOG = Logger.getGlobal()

    fun registerPort(localPort: Int) {
        val targetFile = File(StorageManager.getDefault().storageDir, IPC_PORT_FILENAME)
        targetFile.writeText(localPort.toString())
    }

    fun getRegisteredPort(): Int {
        val targetFile = File(StorageManager.getDefault().storageDir, IPC_PORT_FILENAME)
        if (targetFile.exists()) {
            return targetFile.readText().toInt()
        }

        return -1
    }

    fun sendCommand(type: Int, body: String?) {
        val remotePort = getRegisteredPort()
        val socket = Socket(InetAddress.getLoopbackAddress(), remotePort)

        val dos = DataOutputStream(socket.getOutputStream())
        dos.writeInt(type)

        // Send body
        var bodyLength = 0
        var bodyBytes: ByteArray? = null

        if (body != null) {
            bodyBytes = body.toByteArray(Charset.forName("UTF-8"))
            bodyLength = bodyBytes.size
        }

        dos.writeInt(bodyLength)
        if (bodyLength>0) {
            dos.write(bodyBytes, 0, bodyLength)
        }

        socket.close()
    }

    fun requestRemoteControlPanelOpen() {

    }
}