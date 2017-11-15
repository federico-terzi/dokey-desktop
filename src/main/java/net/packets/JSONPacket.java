package net.packets;

import net.model.KeyboardKeys;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represent a DEPacket with a JSONPayload
 */
public class JSONPacket extends DEPacket {

    private boolean isJsonParsed = false;  // If true, it means that the JSON payload has been parsed.
                                           // This is true after calling the parse() method.

    public JSONPacket(int opType, String payload){
        super(opType, payload);
    }

    public JSONPacket(int opType, byte[] payload){
        super(opType, payload);
    }

    public JSONPacket(int opType, long packetID, byte responseFlag, int payloadLength, byte[] payload) {
        super(opType, packetID, responseFlag, payloadLength, payload);
    }

    /**
     * Parse the JSON payload fields. Must be overridden in each subclass.
     */
    public void parse() {
        isJsonParsed = true;
    }

    /**
     * Check that the payload has been parsed and fields populated.
     * Raise an exception if not true.
     * @throws NotParsedException
     */
    public void checkJsonHasBeenParsed() throws NotParsedException {
        if (!isJsonParsed) {
            throw new NotParsedException("Before using payload values, the JSON must be parsed!");
        }
    }

    /**
     * Thrown when using values before parsing the payload.
     */
    static class NotParsedException extends RuntimeException {
        public NotParsedException(String message) {
            super(message);
        }
    }
}
