package net

import net.packets.DEPacket
import java.io.IOException

/**
 * The Daemon that continuously checks for new packets.
 */
class DEDaemon(private val manager: DEManager) : Thread() {

    var shouldStop : Boolean = false

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
                val response = manager.onPacketEventListener?.onPacketReceived(packet)

                // and send the response packet ( ACK ).
                // ( with a response if available ).
                val responsePacket = if (response == null) {
                    DEPacket.responsePacket(packet)
                }else{
                    DEPacket.responsePacket(packet, response)
                }
                manager.sendPacket(responsePacket)
            }

            return packet
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun run() {
        while (!shouldStop) {
            // Check if there are new packets available
            if (manager.hasNewPackets()) {
                // Receive a packet and trigger the corresponding action.
                val packet = receivePacket()

                if (packet != null) {
                    println(packet)
                }
            }
        }
    }
}
