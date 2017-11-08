package net;

import java.io.*;
import java.net.Socket;

/**
 * The DEManager manages the socket connection and receives/sends DEPackets.
 */
public class DEManager {

    private Socket socket;

    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;

    public DEManager(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    /**
     * Send a DEPacket through the socket.
     * @param packet the DEPacket to send.
     * @throws IOException
     */
    public synchronized void sendPacket(DEPacket packet) throws IOException {
        // Write the packet to the stream
        dataOutputStream.writeInt(packet.getOpType());
        dataOutputStream.writeLong(packet.getPacketID());
        dataOutputStream.write(packet.getResponseFlag());
        dataOutputStream.writeInt(packet.getPayloadLength());
        dataOutputStream.write(packet.getPayload(), 0, packet.getPayloadLength());
    }

    /**
     * Receive a DEPacket from the socket input stream.
     * @return the DEPacket
     * @throws IOException
     */
    public DEPacket receivePacket() throws IOException {
        // Read the packet info
        int opType = dataInputStream.readInt();
        long packetID = dataInputStream.readLong();
        byte responseFlag = dataInputStream.readByte();
        int payloadLength = dataInputStream.readInt();

        // Create the result buffer
        byte[] payload = new byte[payloadLength];

        // Read the payload
        dataInputStream.read(payload, 0, payloadLength);

        // Create the packet and return it
        return new DEPacket(opType, packetID, responseFlag, payloadLength, payload);
    }

    /**
     * Check if there is new data available.
     * @return true if there is, false otherwise.
     */
    public boolean hasNewPackets() {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
