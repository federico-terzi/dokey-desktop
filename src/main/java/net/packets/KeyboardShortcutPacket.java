package net.packets;

import net.model.KeyboardKeys;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represent a keyboard shortcut event.
 */
public class KeyboardShortcutPacket extends JSONPacket {

    public static final int OP_TYPE = 1001;

    // Response codes
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_ERROR = "ER";

    // Payload values, must be populated with the parse() method.
    private String application = null;
    private String keys = null;

    public KeyboardShortcutPacket(String payload){
        super(OP_TYPE, payload);
    }

    public KeyboardShortcutPacket(byte[] payload){
        super(OP_TYPE, payload);
    }

    public KeyboardShortcutPacket(long packetID, byte responseFlag, int payloadLength, byte[] payload) {
        super(OP_TYPE, packetID, responseFlag, payloadLength, payload);
    }

    /**
     * Create a KeyboardShortcutPacket request.
     * @param application String application identifier
     * @param keyCombination keyboard combination as String
     * @throws KeyboardShortcutParseException
     */
    public static KeyboardShortcutPacket createRequest(String application, String keyCombination) throws KeyboardShortcutParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app", application);
        jsonObject.put("keys", cleanKeyCombination(keyCombination));
        String json = jsonObject.toString();
        return new KeyboardShortcutPacket(json);
    }

    /**
     * Format the given keyboard combination and make sure the passed keys are valid.
     * @param keyCombination the keyboard combination to parse
     * @return a list of KeyboardKeys that must be pressed
     * @throws KeyboardShortcutParseException if a passed key is not valid.
     */
    public static String cleanKeyCombination(String keyCombination) throws KeyboardShortcutParseException{
        // Remove spaces and trim
        keyCombination = keyCombination.trim().replace(" ", "");

        // Analyze the string with a tokenizer
        StringTokenizer st = new StringTokenizer(keyCombination, "+");

        StringBuilder sb = new StringBuilder();

        // Cycle through all tokens
        while(st.hasMoreTokens()) {
            // Get the next token, all upper case
            String currentToken = st.nextToken().toUpperCase();

            // Make sure the tokens are not empty.
            if (currentToken.trim().isEmpty()) {
                throw new KeyboardShortcutParseException("Syntax error in the keyboard shortcut.");
            }

            // If the key doesn't exist, raise an exception
            if (!KeyboardKeys.isKeyValid(currentToken)) {
                throw new KeyboardShortcutParseException(currentToken+" is not a valid key!");
            }

            // If not, append it to the string builder
            sb.append(currentToken);
            if (st.hasMoreTokens()) {  // Not the last element
                sb.append("+");
            }
        }

        return sb.toString();
    }

    /**
     * Return the list of KeyboardKeys used in the shortcut.
     * @return the list of KeyboardKeys used in the shortcut.
     */
    public List<KeyboardKeys> getKeys() {
        checkJsonHasBeenParsed();

        List<KeyboardKeys> output = new ArrayList<>();

        // Analyze the string payload with a tokenizer
        StringTokenizer st = new StringTokenizer(keys, "+");

        // Cycle through all tokens
        while(st.hasMoreTokens()) {
            // Get the next token, all upper case
            String currentToken = st.nextToken().toUpperCase();

            // Get the current key
            KeyboardKeys current = KeyboardKeys.findFromName(currentToken);

            // Add it to the list ( if found )
            if (current != null) {
                output.add(current);
            }
        }

        return output;
    }

    /**
     * Parse the payload json values
     */
    @Override
    public void parse() {
        JSONObject jsonObject = new JSONObject(getPayloadAsString());
        application = jsonObject.getString("app");
        keys = jsonObject.getString("keys");
        super.parse();
    }

    public String getApplication() {
        return application;
    }

    public String getKeysString() {
        return keys;
    }

    /**
     * Thrown when parsing a wrong keyboard shortcut.
     */
    static class KeyboardShortcutParseException extends Exception {
        public KeyboardShortcutParseException(String message) {
            super(message);
        }
    }
}
