package net

import net.packets.DEPacket
import java.io.*
import java.net.Socket

/**
 * The DEManager manages the socket connection and receives/sends DEPackets.
 */
open class DEManager @Throws(IOException::class)
    constructor(private val socket: Socket) {

    private val inputStream: InputStream
    private val dataInputStream: DataInputStream
    private val outputStream: OutputStream
    private val dataOutputStream: DataOutputStream

    private var daemon : DEDaemon? = null

    var onPacketEventListener: OnPacketEventListener? = null

    init {
        inputStream = socket.getInputStream()
        outputStream = socket.getOutputStream()
        dataInputStream = DataInputStream(inputStream)
        dataOutputStream = DataOutputStream(outputStream)

        daemon = DEDaemon(this)
    }

    /**
     * Send a DEPacket through the socket.
     * @param packet the DEPacket to send.
     * @return the Packet ID
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    fun sendPacket(packet: DEPacket) : Long {
        // Write the packet to the stream
        dataOutputStream.writeInt(packet.opType)
        dataOutputStream.writeLong(packet.packetID)
        dataOutputStream.write(packet.responseFlag.toInt())
        dataOutputStream.writeInt(packet.payloadLength)
        dataOutputStream.write(packet.payload, 0, packet.payloadLength)

        return packet.packetID
    }

    /**
     * Receive a DEPacket from the socket input stream.
     * @return the DEPacket
     * @throws IOException
     */
    @Throws(IOException::class)
    fun receivePacket(): DEPacket {
        // Read the packet info
        val opType = dataInputStream.readInt()
        val packetID = dataInputStream.readLong()
        val responseFlag = dataInputStream.readByte()
        val payloadLength = dataInputStream.readInt()

        // Create the result buffer
        val payload = ByteArray(payloadLength)

        // Read the payload
        dataInputStream.read(payload, 0, payloadLength)

        // Create the packet and return it
        return DEPacket(opType, packetID, responseFlag, payloadLength, payload)
    }

    /**
     * Check if there is new data available.
     * @return true if there is, false otherwise.
     */
    fun hasNewPackets(): Boolean {
        return try {
            inputStream.available() > 0
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Start the receiving daemon
     */
    fun startDaemon() {
        // If the daemon is not alive, create a new one
        if (!daemon?.isAlive!!) {
            daemon = DEDaemon(this)
        }

        daemon?.start()
    }

    /**
     * Stop the receiving daemon
     */
    fun stopDaemon() {
        daemon?.shouldStop = true
    }

    /**
     * Artificially simulate a receiving cycle of the DEDaemon
     */
    fun forceDaemonReceivePacket() {
        daemon?.receivePacket()
    }

    interface OnPacketEventListener {
        /**
         * Triggered when a packet has been read and processed by the
         * receiver.
         */
        fun onPacketAcknowledged(packet : DEPacket)

        /**
         * Triggered when receiving a request packet.
         */
        fun onPacketReceived(packet : DEPacket)
    }
}
