package net.packets;

import java.util.StringTokenizer;

/**
 * Represent a keyboard shortcut event.
 */
public class KeyboardShortcutPacket extends DEPacket {

    public static final int OP_TYPE = 1001;

    public KeyboardShortcutPacket(String keyCombination) throws KeyboardShortcutParseException {
        super(parseKeyCombination(keyCombination));
        this.setOpType(OP_TYPE);
    }

    /**
     * Parse the given keyboard combination to format it in the correct way
     * @param keyCombination the keyboard combination to parse
     * @return the validated keyboard combination string
     */
    private static String parseKeyCombination(String keyCombination) throws KeyboardShortcutParseException{
        // Remove spaces and trim
        keyCombination = keyCombination.trim().replace(" ", "");

        // Analyze the string with a tokenizer
        StringTokenizer st = new StringTokenizer(keyCombination, "+");

        // TODO

        return null;
    }

    /**
     * Thrown when parsing a wrong keyboard shortcut.
     */
    class KeyboardShortcutParseException extends Exception {
        public KeyboardShortcutParseException(String message) {
            super(message);
        }
    }
}
