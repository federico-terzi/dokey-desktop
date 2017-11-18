package net.packets;

import utils.RandomUtils;

import java.util.Arrays;

/**
 * Represents the fundamental unit to transfer data.
 */
public class DEPacket {
    // Fundamental OP TYPEs
    // Codes between 0 and 1000 are reserved
    public static final int OP_TYPE_NORMAL_PACKET = 1;

    // RESPONSE FLAGS
    public static final byte RESPONSE_FLAG_REQUEST = 0;
    public static final byte RESPONSE_FLAG_RESPONSE = 1;

    // Fields
    private int opType = OP_TYPE_NORMAL_PACKET;
    private long packetID = 0;
    private byte responseFlag = RESPONSE_FLAG_REQUEST;
    private int payloadLength = 0;
    private byte[] payload = new byte[0];

    /**
     * Constructor for building an empty packet.
     */
    public DEPacket() {
        this.packetID = RandomUtils.getNextID();
    }

    /**
     * Constructor for building an empty packet with random ID and custom opType.
     */
    public DEPacket(int opType) {
        this.packetID = RandomUtils.getNextID();
        this.opType = opType;
    }

    /**
     * Constructor for building a request packet with a string payload and random ID.
     */
    public DEPacket(String stringPayload) {
        this.packetID = RandomUtils.getNextID();
        this.responseFlag = DEPacket.RESPONSE_FLAG_REQUEST;
        this.payloadLength = stringPayload.length();
        this.payload = stringPayload.getBytes();
    }

    /**
     * Constructor for building a request packet with a string payload and random ID.
     */
    public DEPacket(int opType, String stringPayload) {
        this.opType = opType;
        this.packetID = RandomUtils.getNextID();
        this.responseFlag = DEPacket.RESPONSE_FLAG_REQUEST;
        this.payloadLength = stringPayload.length();
        this.payload = stringPayload.getBytes();
    }

    /**
     * Constructor for building a request packet with a string payload and random ID.
     */
    public DEPacket(int opType, byte[] payload) {
        this.opType = opType;
        this.packetID = RandomUtils.getNextID();
        this.responseFlag = DEPacket.RESPONSE_FLAG_REQUEST;
        this.payloadLength = payload.length;
        this.payload = payload;
    }

    /**
     * Constructor for building a packet with a string payload and random ID.
     */
    public DEPacket(String stringPayload, byte responseFlag) {
        this.packetID = RandomUtils.getNextID();
        this.responseFlag = responseFlag;
        this.payloadLength = stringPayload.length();
        this.payload = stringPayload.getBytes();
    }

    /**
     * Constructor for building a packet.
     */
    public DEPacket(int opType, long packetID, byte responseFlag, int payloadLength, byte[] payload) {
        this.opType = opType;
        this.packetID = packetID;
        this.responseFlag = responseFlag;
        this.payloadLength = payloadLength;
        this.payload = payload;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public long getPacketID() {
        return packetID;
    }

    public void setPacketID(long packetID) {
        this.packetID = packetID;
    }

    public byte getResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(byte responseFlag) {
        this.responseFlag = responseFlag;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadAsString() {
        return new String(payload);
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setPayload(String stringPayload) {
        this.payload = stringPayload.getBytes();
        this.payloadLength = stringPayload.length();
    }

    public boolean isResponsePacket() {
        return this.responseFlag == RESPONSE_FLAG_RESPONSE;
    }

    /**
     * Convert the current packet into a response packet for the given
     * request packet.
     * @param requestPacket the request DEPacket
     */
    public void convertToResponsePacket(DEPacket requestPacket) {
        setPacketID(requestPacket.getPacketID());
        setOpType(requestPacket.getOpType());
        setResponseFlag(DEPacket.RESPONSE_FLAG_RESPONSE);
    }

    @Override
    public String toString() {
        String payloadString = "<EMPTY>";
        if (payloadLength > 0) {
            payloadString = new String(payload);
        }
        return "DEPacket{" +
                "OT=" + opType +
                ", ID=" + packetID +
                ", RF=" + responseFlag +
                ", PL=" + payloadLength +
                ", P=" + payloadString +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DEPacket packet = (DEPacket) o;

        if (opType != packet.opType) return false;
        if (packetID != packet.packetID) return false;
        if (responseFlag != packet.responseFlag) return false;
        if (payloadLength != packet.payloadLength) return false;
        return payloadLength == 0 || Arrays.equals(payload, packet.payload);
    }

    @Override
    public int hashCode() {
        int result = opType;
        result = 31 * result + (int) (packetID ^ (packetID >>> 32));
        result = 31 * result + (int) responseFlag;
        result = 31 * result + payloadLength;
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    /**
     * Generate and return an empty DEPacket with an unique ID.
     */
    public static DEPacket empty() {
        return new DEPacket();
    }

    /**
     * Generate the response packet for the given request packet.
     */
    public static DEPacket responsePacket(DEPacket requestPacket) {
        DEPacket packet = new DEPacket();
        packet.setPacketID(requestPacket.getPacketID());
        packet.setOpType(requestPacket.getOpType());
        packet.setResponseFlag(DEPacket.RESPONSE_FLAG_RESPONSE);
        return packet;
    }

    /**
     * Generate the response packet for the given request packet and the given response.
     */
    public static DEPacket responsePacket(DEPacket requestPacket, String response) {
        DEPacket packet = new DEPacket();
        packet.setPacketID(requestPacket.getPacketID());
        packet.setOpType(requestPacket.getOpType());
        packet.setResponseFlag(DEPacket.RESPONSE_FLAG_RESPONSE);
        packet.setPayloadLength(response.length());
        packet.setPayload(response.getBytes());
        return packet;
    }

    /**
     * Generate the response packet for the given request packet and the given response.
     */
    public static DEPacket responsePacket(DEPacket requestPacket, byte[] response) {
        DEPacket packet = new DEPacket();
        packet.setPacketID(requestPacket.getPacketID());
        packet.setOpType(requestPacket.getOpType());
        packet.setResponseFlag(DEPacket.RESPONSE_FLAG_RESPONSE);
        packet.setPayloadLength(response.length);
        packet.setPayload(response);
        return packet;
    }

    /**
     * Generate and return a DEPacket with the given string.
     * An ID is also automatically generated.
     */
    public static DEPacket stringPacket(String string) {
        return new DEPacket(string);
    }
}
