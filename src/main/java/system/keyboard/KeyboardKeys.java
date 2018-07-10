package system.keyboard;

import net.model.DeviceInfo;

public enum KeyboardKeys {
    VK_ENTER ("ENTER", "ENTER", "VK_ENTER", 10),
    VK_BACK_SPACE ("BACK_SPACE", "BACKSPACE", "VK_BACK_SPACE", 8),
    VK_TAB ("TAB", "TAB", "VK_TAB", 9),
    VK_CANCEL ("CANC", "CANC", "VK_CANCEL", 3),
    VK_CLEAR ("CLEAR", "CLEAR", "VK_CLEAR", 12),
    VK_SHIFT ("SHIFT", "SHIFT", "VK_SHIFT", 16),
    VK_CONTROL ("CTRL", "CTRL", "VK_CONTROL", 17),
    VK_ALT ("ALT", "ALT", "VK_ALT", 18),
    VK_PAUSE ("PAUSE", "PAUSE", "VK_PAUSE", 19),
    VK_CAPS_LOCK ("CAPS_LOCK", "CAPS_LOCK", "VK_CAPS_LOCK", 20),
    VK_ESCAPE ("ESC", "ESC", "VK_ESCAPE", 27),
    VK_SPACE ("SPACE", "SPACE", "VK_SPACE", 32),
    VK_PAGE_UP ("PAGE_UP", "PAGE_UP", "VK_PAGE_UP", 33),
    VK_PAGE_DOWN ("PAGE_DOWN", "PAGE_DOWN", "VK_PAGE_DOWN", 34),
    VK_END ("END", "END", "VK_END", 35),
    VK_HOME ("HOME", "HOME", "VK_HOME", 36),
    VK_LEFT ("LEFT", "LEFT", "VK_LEFT", 37),
    VK_UP ("UP", "UP", "VK_UP", 38),
    VK_RIGHT ("RIGHT", "RIGHT", "VK_RIGHT", 39),
    VK_DOWN ("DOWN", "DOWN", "VK_DOWN", 40),
    VK_COMMA ("COMMA", "COMMA", "VK_COMMA", 44),
    VK_MINUS ("MINUS", "MINUS", "VK_MINUS", 45),
    VK_PERIOD ("PERIOD", "PERIOD", "VK_PERIOD", 46),
    VK_SLASH ("SLASH", "SLASH", "VK_SLASH", 47),
    VK_0 ("0", "0", "VK_0", 48),
    VK_1 ("1", "1", "VK_1", 49),
    VK_2 ("2", "2", "VK_2", 50),
    VK_3 ("3", "3", "VK_3", 51),
    VK_4 ("4", "4", "VK_4", 52),
    VK_5 ("5", "5", "VK_5", 53),
    VK_6 ("6", "6", "VK_6", 54),
    VK_7 ("7", "7", "VK_7", 55),
    VK_8 ("8", "8", "VK_8", 56),
    VK_9 ("9", "9", "VK_9", 57),
    VK_SEMICOLON ("SEMICOLON", "SEMICOLON", "VK_SEMICOLON", 59),
    VK_EQUALS ("EQUALS", "EQUALS", "VK_EQUALS", 61),
    VK_A ("A", "A", "VK_A", 65),
    VK_B ("B", "B", "VK_B", 66),
    VK_C ("C", "C", "VK_C", 67),
    VK_D ("D", "D", "VK_D", 68),
    VK_E ("E", "E", "VK_E", 69),
    VK_F ("F", "F", "VK_F", 70),
    VK_G ("G", "G", "VK_G", 71),
    VK_H ("H", "H", "VK_H", 72),
    VK_I ("I", "I", "VK_I", 73),
    VK_J ("J", "J", "VK_J", 74),
    VK_K ("K", "K", "VK_K", 75),
    VK_L ("L", "L", "VK_L", 76),
    VK_M ("M", "M", "VK_M", 77),
    VK_N ("N", "N", "VK_N", 78),
    VK_O ("O", "O", "VK_O", 79),
    VK_P ("P", "P", "VK_P", 80),
    VK_Q ("Q", "Q", "VK_Q", 81),
    VK_R ("R", "R", "VK_R", 82),
    VK_S ("S", "S", "VK_S", 83),
    VK_T ("T", "T", "VK_T", 84),
    VK_U ("U", "U", "VK_U", 85),
    VK_V ("V", "V", "VK_V", 86),
    VK_W ("W", "W", "VK_W", 87),
    VK_X ("X", "X", "VK_X", 88),
    VK_Y ("Y", "Y", "VK_Y", 89),
    VK_Z ("Z", "Z", "VK_Z", 90),
    VK_OPEN_BRACKET ("OPEN_BRACKET", "OPEN_BRACKET", "VK_OPEN_BRACKET", 91),
    VK_BACK_SLASH ("BACK_SLASH", "BACK_SLASH", "VK_BACK_SLASH", 92),
    VK_CLOSE_BRACKET ("CLOSE_BRACKET", "CLOSE_BRACKET", "VK_CLOSE_BRACKET", 93),
    VK_NUMPAD0 ("NUMPAD 0", "NUMPAD 0", "VK_NUMPAD0", 96),
    VK_NUMPAD1 ("NUMPAD 1", "NUMPAD 1", "VK_NUMPAD1", 97),
    VK_NUMPAD2 ("NUMPAD 2", "NUMPAD 2", "VK_NUMPAD2", 98),
    VK_NUMPAD3 ("NUMPAD 3", "NUMPAD 3", "VK_NUMPAD3", 99),
    VK_NUMPAD4 ("NUMPAD 4", "NUMPAD 4", "VK_NUMPAD4", 100),
    VK_NUMPAD5 ("NUMPAD 5", "NUMPAD 5", "VK_NUMPAD5", 101),
    VK_NUMPAD6 ("NUMPAD 6", "NUMPAD 6", "VK_NUMPAD6", 102),
    VK_NUMPAD7 ("NUMPAD 7", "NUMPAD 7", "VK_NUMPAD7", 103),
    VK_NUMPAD8 ("NUMPAD 8", "NUMPAD 8", "VK_NUMPAD8", 104),
    VK_NUMPAD9 ("NUMPAD 9", "NUMPAD 9", "VK_NUMPAD9", 105),
    VK_MULTIPLY ("MULTIPLY", "MULTIPLY", "VK_MULTIPLY", 106),
    VK_ADD ("ADD", "ADD", "VK_ADD", 107),
    VK_SEPARATER ("SEPARATER", "SEPARATER", "VK_SEPARATER", 108),
    VK_SUBTRACT ("SUBTRACT", "SUBTRACT", "VK_SUBTRACT", 109),
    VK_DECIMAL ("DECIMAL", "DECIMAL", "VK_DECIMAL", 110),
    VK_DIVIDE ("DIVIDE", "DIVIDE", "VK_DIVIDE", 111),
    VK_DELETE ("DELETE", "DELETE", "VK_DELETE", 127),
    VK_NUM_LOCK ("NUM_LOCK", "NUM_LOCK", "VK_NUM_LOCK", 144),
    VK_SCROLL_LOCK ("SCROLL_LOCK", "SCROLL_LOCK", "VK_SCROLL_LOCK", 145),
    VK_F1 ("F1", "F1", "VK_F1", 112),
    VK_F2 ("F2", "F2", "VK_F2", 113),
    VK_F3 ("F3", "F3", "VK_F3", 114),
    VK_F4 ("F4", "F4", "VK_F4", 115),
    VK_F5 ("F5", "F5", "VK_F5", 116),
    VK_F6 ("F6", "F6", "VK_F6", 117),
    VK_F7 ("F7", "F7", "VK_F7", 118),
    VK_F8 ("F8", "F8", "VK_F8", 119),
    VK_F9 ("F9", "F9", "VK_F9", 120),
    VK_F10 ("F10", "F10", "VK_F10", 121),
    VK_F11 ("F11", "F11", "VK_F11", 122),
    VK_F12 ("F12", "F12", "VK_F12", 123),
    VK_F13 ("F13", "F13", "VK_F13", 61440),
    VK_F14 ("F14", "F14", "VK_F14", 61441),
    VK_F15 ("F15", "F15", "VK_F15", 61442),
    VK_F16 ("F16", "F16", "VK_F16", 61443),
    VK_F17 ("F17", "F17", "VK_F17", 61444),
    VK_F18 ("F18", "F18", "VK_F18", 61445),
    VK_F19 ("F19", "F19", "VK_F19", 61446),
    VK_F20 ("F20", "F20", "VK_F20", 61447),
    VK_F21 ("F21", "F21", "VK_F21", 61448),
    VK_F22 ("F22", "F22", "VK_F22", 61449),
    VK_F23 ("F23", "F23", "VK_F23", 61450),
    VK_F24 ("F24", "F24", "VK_F24", 61451),
    VK_PRINTSCREEN ("PRINTSCREEN", "PRINTSCREEN", "VK_PRINTSCREEN", 154),
    VK_INSERT ("INSERT", "INSERT", "VK_INSERT", 155),
    VK_HELP ("HELP", "HELP", "VK_HELP", 156),
    VK_META ("META", "META", "VK_META", 157),
    VK_WINDOWS ("WIN", "WINDOWS", "VK_WINDOWS", 524),
    VK_COMMAND ("CMD", "COMMAND", "VK_COMMAND", 157),
    VK_BACK_QUOTE ("BACK_QUOTE", "BACK_QUOTE", "VK_BACK_QUOTE", 192),
    VK_QUOTE ("QUOTE", "QUOTE", "VK_QUOTE", 222),
    VK_KP_UP ("KP_UP", "KP_UP", "VK_KP_UP", 224),
    VK_KP_DOWN ("KP_DOWN", "KP_DOWN", "VK_KP_DOWN", 225),
    VK_KP_LEFT ("KP_LEFT", "KP_LEFT", "VK_KP_LEFT", 226),
    VK_KP_RIGHT ("KP_RIGHT", "KP_RIGHT", "VK_KP_RIGHT", 227),
    VK_DEAD_GRAVE ("DEAD_GRAVE", "DEAD_GRAVE", "VK_DEAD_GRAVE", 128),
    VK_DEAD_ACUTE ("DEAD_ACUTE", "DEAD_ACUTE", "VK_DEAD_ACUTE", 129),
    VK_DEAD_CIRCUMFLEX ("DEAD_CIRCUMFLEX", "DEAD_CIRCUMFLEX", "VK_DEAD_CIRCUMFLEX", 130),
    VK_DEAD_TILDE ("DEAD_TILDE", "DEAD_TILDE", "VK_DEAD_TILDE", 131),
    VK_DEAD_MACRON ("DEAD_MACRON", "DEAD_MACRON", "VK_DEAD_MACRON", 132),
    VK_DEAD_BREVE ("DEAD_BREVE", "DEAD_BREVE", "VK_DEAD_BREVE", 133),
    VK_DEAD_ABOVEDOT ("DEAD_ABOVEDOT", "DEAD_ABOVEDOT", "VK_DEAD_ABOVEDOT", 134),
    VK_DEAD_DIAERESIS ("DEAD_DIAERESIS", "DEAD_DIAERESIS", "VK_DEAD_DIAERESIS", 135),
    VK_DEAD_ABOVERING ("DEAD_ABOVERING", "DEAD_ABOVERING", "VK_DEAD_ABOVERING", 136),
    VK_DEAD_DOUBLEACUTE ("DEAD_DOUBLEACUTE", "DEAD_DOUBLEACUTE", "VK_DEAD_DOUBLEACUTE", 137),
    VK_DEAD_CARON ("DEAD_CARON", "DEAD_CARON", "VK_DEAD_CARON", 138),
    VK_DEAD_CEDILLA ("DEAD_CEDILLA", "DEAD_CEDILLA", "VK_DEAD_CEDILLA", 139),
    VK_DEAD_OGONEK ("DEAD_OGONEK", "DEAD_OGONEK", "VK_DEAD_OGONEK", 140),
    VK_DEAD_IOTA ("DEAD_IOTA", "DEAD_IOTA", "VK_DEAD_IOTA", 141),
    VK_DEAD_VOICED_SOUND ("DEAD_VOICED_SOUND", "DEAD_VOICED_SOUND", "VK_DEAD_VOICED_SOUND", 142),
    VK_DEAD_SEMIVOICED_SOUND ("DEAD_SEMIVOICED_SOUND", "DEAD_SEMIVOICED_SOUND", "VK_DEAD_SEMIVOICED_SOUND", 143),
    VK_AMPERSAND ("AMPERSAND", "AMPERSAND", "VK_AMPERSAND", 150),
    VK_ASTERISK ("ASTERISK", "ASTERISK", "VK_ASTERISK", 151),
    VK_QUOTEDBL ("QUOTEDBL", "QUOTEDBL", "VK_QUOTEDBL", 152),
    VK_LESS ("LESS", "LESS", "VK_LESS", 153),
    VK_GREATER ("GREATER", "GREATER", "VK_GREATER", 160),
    VK_BRACELEFT ("BRACELEFT", "BRACELEFT", "VK_BRACELEFT", 161),
    VK_BRACERIGHT ("BRACERIGHT", "BRACERIGHT", "VK_BRACERIGHT", 162),
    VK_AT ("AT", "AT", "VK_AT", 512),
    VK_COLON ("COLON", "COLON", "VK_COLON", 513),
    VK_CIRCUMFLEX ("CIRCUMFLEX", "CIRCUMFLEX", "VK_CIRCUMFLEX", 514),
    VK_DOLLAR ("DOLLAR", "DOLLAR", "VK_DOLLAR", 515),
    VK_EURO_SIGN ("EURO_SIGN", "EURO_SIGN", "VK_EURO_SIGN", 516),
    VK_EXCLAMATION_MARK ("EXCLAMATION_MARK", "EXCLAMATION_MARK", "VK_EXCLAMATION_MARK", 517),
    VK_INVERTED_EXCLAMATION_MARK ("INVERTED_EXCLAMATION_MARK", "INVERTED_EXCLAMATION_MARK", "VK_INVERTED_EXCLAMATION_MARK", 518),
    VK_LEFT_PARENTHESIS ("LEFT_PARENTHESIS", "LEFT_PARENTHESIS", "VK_LEFT_PARENTHESIS", 519),
    VK_NUMBER_SIGN ("NUMBER_SIGN", "NUMBER_SIGN", "VK_NUMBER_SIGN", 520),
    VK_PLUS ("PLUS", "PLUS", "VK_PLUS", 521),
    VK_RIGHT_PARENTHESIS ("RIGHT_PARENTHESIS", "RIGHT_PARENTHESIS", "VK_RIGHT_PARENTHESIS", 522),
    VK_UNDERSCORE ("UNDERSCORE", "UNDERSCORE", "VK_UNDERSCORE", 523),
    VK_CONTEXT_MENU ("CONTEXT_MENU", "CONTEXT_MENU", "VK_CONTEXT_MENU", 525),
    VK_FINAL ("FINAL", "FINAL", "VK_FINAL", 24),
    VK_CONVERT ("CONVERT", "CONVERT", "VK_CONVERT", 28),
    VK_NONCONVERT ("NONCONVERT", "NONCONVERT", "VK_NONCONVERT", 29),
    VK_ACCEPT ("ACCEPT", "ACCEPT", "VK_ACCEPT", 30),
    VK_MODECHANGE ("MODECHANGE", "MODECHANGE", "VK_MODECHANGE", 31);

    private final String keyName;
    private final String convertName;  // Used for the conversion between JavaFx KeyEvents and AWT ones
    private final String keyEventName;
    private final int keyCode;

    KeyboardKeys(String keyName, String convertName, String keyEventName, int keyCode) {
        this.keyName = keyName;
        this.convertName = convertName;
        this.keyEventName = keyEventName;
        this.keyCode = keyCode;
    }

    public static boolean isKeyValid(String keyName) {
        for (KeyboardKeys key : KeyboardKeys.values()) {
            if (key.keyName.equals(keyName)) {
                return true;
            }
        }
        return false;
    }

    public static KeyboardKeys findFromName(String keyName) {
        for (KeyboardKeys key : KeyboardKeys.values()) {
            if (key.keyName.equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    public static KeyboardKeys findFromConvertedName(String keyName) {
        for (KeyboardKeys key : KeyboardKeys.values()) {
            if (key.convertName.equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    public static KeyboardKeys findFromCode(int keyCode) {
        for (KeyboardKeys key : KeyboardKeys.values()) {
            if (key.keyCode == keyCode) {
                return key;
            }
        }
        return null;
    }

    public String getKeyName() {
        return keyName;
    }

    /**
     * Return a key name based on the operating system.
     */
    public String getKeyName(DeviceInfo.OS os) {
        return keyName;
    }

    public String getKeyEventName() {
        return keyEventName;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getKeyCode(DeviceInfo.OS os) {
        return keyCode;
    }

    public String getConvertName() {
        return convertName;
    }
}