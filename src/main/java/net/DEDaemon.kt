package net

import java.io.IOException

/**
 * The Daemon that continuously checks for new packets.
 */
class DEDaemon(private val manager: DEManager) : Thread() {

    var shouldStop : Boolean = false

    override fun run() {
        while (!shouldStop) {
            // Check if there are new packets available
            if (manager.hasNewPackets()) {
                // Read the packet
                try {
                    val packet = manager.receivePacket()

                    // If the packet is a response packet, trigger the callback
                    if (packet.isResponsePacket) {
                        manager.onPacketReceivedListener?.onPacketReceived(packet)
                    }else{  // Request packet
                        // Send the acknowledgement
                        val responsePacket = DEPacketFactory.generateResponsePacket(packet)
                        manager.sendPacket(responsePacket, false)
                    }

                    println(packet)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
