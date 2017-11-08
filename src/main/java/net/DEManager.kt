package net

import utils.RandomUtils

import java.io.*
import java.net.Socket

/**
 * The DEManager manages the socket connection and receives/sends DEPackets.
 */
class DEManager @Throws(IOException::class)
    constructor(private val socket: Socket) {

    private val inputStream: InputStream
    private val dataInputStream: DataInputStream
    private val outputStream: OutputStream
    private val dataOutputStream: DataOutputStream

    private var daemon : DEDaemon? = null

    var onPacketReceivedListener : OnPacketReceivedListener? = null

    init {
        inputStream = socket.getInputStream()
        outputStream = socket.getOutputStream()
        dataInputStream = DataInputStream(inputStream)
        dataOutputStream = DataOutputStream(outputStream)

        daemon = DEDaemon(this)
    }

    /**
     * Send a DEPacket through the socket. If generateID is true, the PacketID will
     * be automatically generated.
     * @param packet the DEPacket to send.
     * @return the Packet ID
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    fun sendPacket(packet: DEPacket, generateID: Boolean = true) : Long {
        val packetID: Long = if (generateID) {  // Generate the ID automatically
            RandomUtils.getNextID()
        } else {  // Use the one specified in the Packet
            packet.packetID
        }

        // Write the packet to the stream
        dataOutputStream.writeInt(packet.opType)
        dataOutputStream.writeLong(packetID)
        dataOutputStream.write(packet.responseFlag.toInt())
        dataOutputStream.writeInt(packet.payloadLength)
        dataOutputStream.write(packet.payload, 0, packet.payloadLength)

        return packetID
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

    interface OnPacketReceivedListener {
        fun onPacketReceived(packet : DEPacket)
    }
}
