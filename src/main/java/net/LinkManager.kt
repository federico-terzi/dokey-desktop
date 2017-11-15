package net

import net.packets.DEPacket
import net.packets.KeyboardShortcutPacket
import java.net.Socket
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * The LinkManager manages the transmission of complex data between receivers.
 */
class LinkManager(val socket: Socket) : DEManager(socket) {

    private val packetTypes = HashMap<Int, Class<out DEPacket>>()

    init {
        // Register the Packet types in the map
        registerPacketTypes()
    }

    /**
     * Called once by the constructor to initialize packet types
     */
    private fun registerPacketTypes() {
        packetTypes.put(KeyboardShortcutPacket.OP_TYPE, KeyboardShortcutPacket::class.java)
    }

    /**
     * Return an instance of the specialized packet corresponding to the give opType.
     */
    fun getSpecializedPacket(packet: DEPacket): DEPacket? {
        if (packetTypes.containsKey(packet.opType)) {
            return packetTypes[packet.opType]?.getConstructor(Long::class.java, Byte::class.java,
                    Int::class.java, ByteArray::class.java)
                    ?.newInstance(packet.packetID, packet.responseFlag, packet.payloadLength, packet.payload)
        }

        return null
    }
}