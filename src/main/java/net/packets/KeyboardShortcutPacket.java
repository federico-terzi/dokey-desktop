package net.packets;

/**
 * Represent a keyboard shortcut event.
 */
public class KeyboardShortcutPacket extends DEPacket {

    public static final int OP_TYPE = 1001;

    public KeyboardShortcutPacket(String keyCombination) {
        super(parseKeyCombination(keyCombination));
        this.setOpType(OP_TYPE);
    }

    /**
     * Parse the given keyboard combination to format it in the correct way
     * @param keyCombination the keyboard combination to parse
     * @return the validated keyboard combination string
     */
    private static String parseKeyCombination(String keyCombination) {
        return null;
    }
}
