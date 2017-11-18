package net

import net.model.KeyboardKeys
import net.model.RemoteApplication
import net.packets.AppListPacket
import net.packets.DEPacket
import net.packets.KeyboardShortcutPacket
import java.net.Socket

/**
 * The LinkManager manages the transmission of complex data between receivers.
 */
class LinkManager(val socket: Socket) : DEManager(socket), DEManager.OnPacketEventListener {
    // Used to generate an instance of the specialized packet from the OP_TYPE
    private val packetTypes = HashMap<Int, Class<out DEPacket>>()

    // Used to keep track of all the acknowledgment callbacks from sent packets
    private val ackCallbackMap = HashMap<Long, OnPacketAcknowledgedListener>()

    // Used to keep track of the listeners for specific types of packets
    private val eventCallbackMap = HashMap<Int, OnPacketReceivedListener>()

    init {
        // Register the Packet types in the map
        registerPacketTypes()

        this.onPacketEventListener = this
    }

    /**
     * Called once by the constructor to initialize packet types
     */
    private fun registerPacketTypes() {
        // Register here all the specialized packet types
        packetTypes.put(KeyboardShortcutPacket.OP_TYPE, KeyboardShortcutPacket::class.java)
        packetTypes.put(AppListPacket.OP_TYPE, AppListPacket::class.java)
    }

    /**
     * Return an instance of the specialized packet corresponding to the give opType.
     */
    fun getSpecializedPacket(packet: DEPacket): DEPacket {
        if (packetTypes.containsKey(packet.opType)) {
            val specializedPacket = packetTypes[packet.opType]?.getConstructor(Long::class.java,
                    Byte::class.java, Int::class.java, ByteArray::class.java)
                    ?.newInstance(packet.packetID, packet.responseFlag, packet.payloadLength, packet.payload)

            // Make sure the specializedPacket exists
            if (specializedPacket != null) {
                return specializedPacket
            }
        }

        // If the packet is not registered in the map, return the raw packet
        return packet
    }

    /**
     * Implemented from the DEManager, used to keep track of all the packets.
     */
    override fun onPacketAcknowledged(packet: DEPacket) {
        // Get the specialized packet.
        val specializedPacket = getSpecializedPacket(packet)

        // Get the callback.
        val listener = ackCallbackMap[packet.packetID]

        // If there is a callback registered, call it.
        if (listener != null) {
            ackCallbackMap.remove(packet.packetID)  // Remove the callback from the map
            listener.onPacketAcknowledged(specializedPacket)  // Call the callback
        }
    }

    /**
     * Implemented from the DEManager, used to keep track of all the packets.
     */
    override fun onPacketReceived(packet: DEPacket): DEPacket? {
        // Get the specialized packet.
        val specializedPacket = getSpecializedPacket(packet)

        // Get the callback.
        val listener = eventCallbackMap[packet.opType]

        // If there is a callback registered, call it.
        if (listener != null) {
            return listener.onPacketReceived(specializedPacket)
        }

        return null
    }

    /**
     * Send a packet and specify an acknowledgment callback
     */
    fun sendPacket(packet: DEPacket, listener: OnPacketAcknowledgedListener) {
        // Send the packet
        this.sendPacket(packet)

        // Register the ack callback event listener
        this.ackCallbackMap.put(packet.packetID, listener)
    }

    /**
     * Register the callback for a specific opType incoming packet
     */
    fun setEventListener(opType: Int, listener: OnPacketReceivedListener) {
        eventCallbackMap[opType] = listener
    }

    /**
     * Remove the event listener.
     */
    fun removeEventListener(opType: Int) {
        if (eventCallbackMap.containsKey(opType)) {
            eventCallbackMap.remove(opType)
        }
    }

    /**
     * Used when sending packets to intercept the callback
     */
    interface OnPacketAcknowledgedListener {
        /**
         * Triggered when a packet has been read and processed by the
         * receiver.
         */
        fun onPacketAcknowledged(packet: DEPacket)
    }

    /**
     * Used when receiving event packets from another host.
     */
    interface OnPacketReceivedListener {
        /**
         * Triggered when receiving a request packet.
         */
        fun onPacketReceived(packet: DEPacket): DEPacket?
    }

    /**
     * SPECIFIC APP FUNCTIONS
     */

    /**
     * KEYBOARD SHORTCUTS
     */

    /**
     * Used to send a keyboard shortcut to the remote server
     */
    fun sendKeyboardShortcut(application: String, keys: String, listener: OnKeyboardShortcutAcknowledgedListener) {
        val packet = KeyboardShortcutPacket.createRequest(application, keys)
        sendPacket(packet, object : OnPacketAcknowledgedListener {
            override fun onPacketAcknowledged(packet: DEPacket) {
                val response = packet.payloadAsString
                listener.onKeyboardShortcutAcknowledged(response)
            }
        })
    }

    /**
     * Used in the "sendKeyboardShortcut" method, called when a shortcut has been
     * received by the server.
     */
    interface OnKeyboardShortcutAcknowledgedListener {
        fun onKeyboardShortcutAcknowledged(response: String)
    }

    /**
     * Set a keyboard shortcut listener to receive keyboard shortcut events.
     */
    fun setKeyboardShortcutListener(listener: OnKeyboardShortcutReceivedListener) {
        setEventListener(KeyboardShortcutPacket.OP_TYPE, object : OnPacketReceivedListener {
            override fun onPacketReceived(packet: DEPacket): DEPacket? {
                val keyPacket = packet as KeyboardShortcutPacket  // Cast the packet
                keyPacket.parse()  // Parse the values
                val result = listener.onKeyboardShortcutReceived(keyPacket.application, keyPacket.keys)
                val finalResponse = if (result) {
                    KeyboardShortcutPacket.RESPONSE_OK
                } else {
                    KeyboardShortcutPacket.RESPONSE_ERROR
                }
                return DEPacket.responsePacket(packet, finalResponse)
            }
        })
    }

    interface OnKeyboardShortcutReceivedListener {
        fun onKeyboardShortcutReceived(application: String, keys: List<KeyboardKeys>): Boolean
    }

    /**
     * APPLICATION LIST REQUEST
     */

    /**
     * Request a list of apps to the server
     */
    fun requestAppList(listener: OnAppListResponseListener) {
        val packet = AppListPacket.createRequest();
        sendPacket(packet, object : OnPacketAcknowledgedListener {
            override fun onPacketAcknowledged(packet: DEPacket) {
                val appListPacket = packet as AppListPacket  // Cast the packet
                appListPacket.parse()  // Parse the values
                // Send the received list of apps
                listener.onAppListResponceReceived(appListPacket.applications)
            }
        })
    }


    interface OnAppListResponseListener {
        fun onAppListResponceReceived(apps : List<RemoteApplication> )
    }

    fun setAppListRequestListener(listener: OnAppListRequestListener) {
        setEventListener(AppListPacket.OP_TYPE, object : OnPacketReceivedListener {
            override fun onPacketReceived(packet: DEPacket): DEPacket? {
                val apps = listener.onAppListRequestReceived()
                val resPacket = AppListPacket.createResponse(packet, apps)
                return resPacket
            }
        })
    }

    interface OnAppListRequestListener {
        fun onAppListRequestReceived(): List<RemoteApplication>
    }
}