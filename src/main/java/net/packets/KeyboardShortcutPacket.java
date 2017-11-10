package net.packets;

import net.model.KeyboardKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represent a keyboard shortcut event.
 */
public class KeyboardShortcutPacket extends DEPacket {

    public static final int OP_TYPE = 1001;

    public KeyboardShortcutPacket(String keyCombination) throws KeyboardShortcutParseException {
        super(cleanKeyCombination(keyCombination));
        this.setOpType(OP_TYPE);
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
        List<KeyboardKeys> output = new ArrayList<>();

        // Analyze the string payload with a tokenizer
        StringTokenizer st = new StringTokenizer(getPayloadAsString(), "+");

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
     * Thrown when parsing a wrong keyboard shortcut.
     */
    static class KeyboardShortcutParseException extends Exception {
        public KeyboardShortcutParseException(String message) {
            super(message);
        }
    }
}
