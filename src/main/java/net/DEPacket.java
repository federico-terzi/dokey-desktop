package net;

import java.io.UnsupportedEncodingException;

/**
 * Represents the fundamental unit to transfer data.
 */
public class DEPacket {
    // OP TYPEs
    public static final int OP_TYPE_NORMAL_PACKET = 1;

    // RESPONSE FLAGS
    public static final byte RESPONSE_FLAG_REQUEST = 0;
    public static final byte RESPONSE_FLAG_RESPONSE = 1;

    // Fields
    private int opType;
    private long packetID;
    private byte responseFlag;
    private int payloadLength;
    private byte[] payload;

    public DEPacket() {
    }

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

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public boolean isResponsePacket() {
        return this.responseFlag == RESPONSE_FLAG_RESPONSE;
    }

    @Override
    public String toString() {
        return "DEPacket{" +
                "OT=" + opType +
                ", ID=" + packetID +
                ", RF=" + responseFlag +
                ", PL=" + payloadLength +
                ", P=" + new String(payload) +
                '}';
    }
}
