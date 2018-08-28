package system.server

import java.io.IOException
import java.net.ServerSocket
import java.util.logging.Logger

const val MIN_PORT = 60642
const val MAX_PORT = 60652

class SocketBuilder {
    companion object {
        val LOG = Logger.getGlobal()

        @JvmStatic
        fun buildSocket() : ServerSocket? {
            var currentPort = MIN_PORT
            while (currentPort <= MAX_PORT) {
                try {
                    val serverSocket = ServerSocket(currentPort)
                    return serverSocket
                }catch (e: Exception) {
                    LOG.warning("Failed to open socket with port: $currentPort, trying the next one...")
                }

                currentPort++
            }

            LOG.severe("Cannot open socket in default port range, aborting...")
            System.exit(4)
            return null
        }
    }
}