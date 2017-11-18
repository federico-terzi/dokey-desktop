package net

import net.packets.DEPacket
import java.io.IOException

/**
 * The Daemon that continuously checks for new packets.
 */
class DEDaemon(private val manager: DEManager, val connectionListener : OnConnectionClosedListener?,
               val verbose : Boolean = false) : Thread() {

    companion object {
        val DEFAULT_CHECK_INTERVAL : Long = 10  // How often check for new packets ( in milliseconds )
    }

    var shouldStop : Boolean = false
    var checkInterval = DEFAULT_CHECK_INTERVAL  // How often check for new packets ( in milliseconds )

    /**
     * Receive a packet and trigger the corresponding actions.
     */
    fun receivePacket() : DEPacket? {
        // Read the packet
        try {
            // Receive the packet
            val packet = manager.receivePacket()

            // If the packet is a response packet, trigger the callback
            if (packet.isResponsePacket) {
                manager.onPacketEventListener?.onPacketAcknowledged(packet)
            }else{  // Request packet
                // If the packet is a request packet,
                // trigger the callback and get the response from it.
                var responsePacket = manager.onPacketEventListener?.onPacketReceived(packet)

                // If response packet is null, createResponse the default one
                if (responsePacket == null) {
                    responsePacket = DEPacket.responsePacket(packet)
                }

                // and send the response packet ( ACK ).
                manager.sendPacket(responsePacket!!)
            }

            return packet
        } catch (e: IOException) {
            e.printStackTrace()
            // Send the connection closed signal
            connectionListener?.onConnectionClosed()

            // Close the daemon
            shouldStop = true
        }
        return null
    }

    override fun run() {
        while (!shouldStop) {
            // Receive a packet and trigger the corresponding action.
            val packet = receivePacket()

            if (packet != null && verbose) {
                println(packet)
            }
            Thread.sleep(checkInterval)
        }
    }

    /**
     * Used to notify when the connection closes
     */
    interface OnConnectionClosedListener {
        fun onConnectionClosed()
    }
}
