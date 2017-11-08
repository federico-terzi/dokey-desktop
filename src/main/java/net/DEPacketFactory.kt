package net

import utils.RandomUtils

class DEPacketFactory {
    companion object {
        /**
         * Generate and return an empty DEPacket with an unique ID.
         */
        fun generateEmptyPacket() : DEPacket {
            val packetID : Long = RandomUtils.getNextID()
            val packet : DEPacket = DEPacket()
            packet.packetID = packetID
            return packet
        }

        /**
         * Generate the response packet for the given request packet.
         */
        fun generateResponsePacket(requestPacket : DEPacket) : DEPacket {
            val packet : DEPacket = DEPacket()
            packet.packetID = requestPacket.packetID
            packet.responseFlag = DEPacket.RESPONSE_FLAG_RESPONSE
            return packet
        }
    }
}