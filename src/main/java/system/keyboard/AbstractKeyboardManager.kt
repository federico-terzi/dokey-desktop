package system.keyboard

import java.awt.Robot
import java.util.*
import java.util.logging.Logger

abstract class AbstractKeyboardManager : KeyboardManager {
    companion object {
        val LOG = Logger.getGlobal()
    }

    val keyCodeMap = mutableMapOf<String, Int>()
    val robot : Robot = Robot()

    init {
        initializeMap()

        // Initialize the AWT robot
        robot.autoDelay = 40
        robot.isAutoWaitForIdle = true
    }

    override fun sendKeystroke(keyStroke: String) {
        // Convert the string keystroke into an array of keycodes
        val keyCodeList = parseKeyCodeList(keyStroke)

        // Press all the keys
        for (keyCode in keyCodeList) {
            robot.delay(10)
            pressKey(keyCode)
        }

        // Release all the keys
        for (keyCode in keyCodeList) {
            robot.delay(10)
            releaseKey(keyCode)
        }
    }

    protected open fun pressKey(keyCode: Int) {
        robot.keyPress(keyCode)
    }

    protected open fun releaseKey(keyCode: Int) {
        robot.keyRelease(keyCode)
    }

    private fun parseKeyCodeList(keyStroke: String) : List<Int> {
        val tokenizer = StringTokenizer(keyStroke, "+")
        val keyCodeList = mutableListOf<Int>()
        while(tokenizer.hasMoreTokens()) {
            val key = tokenizer.nextToken()
            val keyCode = keyCodeMap[key]
            if (keyCode != null) {
                keyCodeList.add(keyCode)
            }else{
                LOG.warning("KeyCode not found for KEY: $key")
            }
        }
        return keyCodeList
    }

    private fun initializeMap() {
        keyCodeMap["ENTER"] = 10
        keyCodeMap["BACK_SPACE"] = 8
        keyCodeMap["TAB"] = 9
        keyCodeMap["CANC"] = 3
        keyCodeMap["CLEAR"] = 12
        keyCodeMap["SHIFT"] = 16
        keyCodeMap["CTRL"] = 17
        keyCodeMap["ALT"] = 18
        keyCodeMap["PAUSE"] = 19
        keyCodeMap["CAPS_LOCK"] = 20
        keyCodeMap["ESC"] = 27
        keyCodeMap["SPACE"] = 32
        keyCodeMap["PAGE_UP"] = 33
        keyCodeMap["PAGE_DOWN"] = 34
        keyCodeMap["END"] = 35
        keyCodeMap["HOME"] = 36
        keyCodeMap["LEFT"] = 37
        keyCodeMap["UP"] = 38
        keyCodeMap["RIGHT"] = 39
        keyCodeMap["DOWN"] = 40
        keyCodeMap["COMMA"] = 44
        keyCodeMap["MINUS"] = 45
        keyCodeMap["PERIOD"] = 46
        keyCodeMap["SLASH"] = 47
        keyCodeMap["0"] = 48
        keyCodeMap["1"] = 49
        keyCodeMap["2"] = 50
        keyCodeMap["3"] = 51
        keyCodeMap["4"] = 52
        keyCodeMap["5"] = 53
        keyCodeMap["6"] = 54
        keyCodeMap["7"] = 55
        keyCodeMap["8"] = 56
        keyCodeMap["9"] = 57
        keyCodeMap["SEMICOLON"] = 59
        keyCodeMap["EQUALS"] = 61
        keyCodeMap["A"] = 65
        keyCodeMap["B"] = 66
        keyCodeMap["C"] = 67
        keyCodeMap["D"] = 68
        keyCodeMap["E"] = 69
        keyCodeMap["F"] = 70
        keyCodeMap["G"] = 71
        keyCodeMap["H"] = 72
        keyCodeMap["I"] = 73
        keyCodeMap["J"] = 74
        keyCodeMap["K"] = 75
        keyCodeMap["L"] = 76
        keyCodeMap["M"] = 77
        keyCodeMap["N"] = 78
        keyCodeMap["O"] = 79
        keyCodeMap["P"] = 80
        keyCodeMap["Q"] = 81
        keyCodeMap["R"] = 82
        keyCodeMap["S"] = 83
        keyCodeMap["T"] = 84
        keyCodeMap["U"] = 85
        keyCodeMap["V"] = 86
        keyCodeMap["W"] = 87
        keyCodeMap["X"] = 88
        keyCodeMap["Y"] = 89
        keyCodeMap["Z"] = 90
        keyCodeMap["OPEN_BRACKET"] = 91
        keyCodeMap["BACK_SLASH"] = 92
        keyCodeMap["CLOSE_BRACKET"] = 93
        keyCodeMap["NUMPAD 0"] = 96
        keyCodeMap["NUMPAD 1"] = 97
        keyCodeMap["NUMPAD 2"] = 98
        keyCodeMap["NUMPAD 3"] = 99
        keyCodeMap["NUMPAD 4"] = 100
        keyCodeMap["NUMPAD 5"] = 101
        keyCodeMap["NUMPAD 6"] = 102
        keyCodeMap["NUMPAD 7"] = 103
        keyCodeMap["NUMPAD 8"] = 104
        keyCodeMap["NUMPAD 9"] = 105
        keyCodeMap["MULTIPLY"] = 106
        keyCodeMap["ADD"] = 107
        keyCodeMap["SEPARATER"] = 108
        keyCodeMap["SUBTRACT"] = 109
        keyCodeMap["DECIMAL"] = 110
        keyCodeMap["DIVIDE"] = 111
        keyCodeMap["DELETE"] = 127
        keyCodeMap["NUM_LOCK"] = 144
        keyCodeMap["SCROLL_LOCK"] = 145
        keyCodeMap["F1"] = 112
        keyCodeMap["F2"] = 113
        keyCodeMap["F3"] = 114
        keyCodeMap["F4"] = 115
        keyCodeMap["F5"] = 116
        keyCodeMap["F6"] = 117
        keyCodeMap["F7"] = 118
        keyCodeMap["F8"] = 119
        keyCodeMap["F9"] = 120
        keyCodeMap["F10"] = 121
        keyCodeMap["F11"] = 122
        keyCodeMap["F12"] = 123
        keyCodeMap["F13"] = 61440
        keyCodeMap["F14"] = 61441
        keyCodeMap["F15"] = 61442
        keyCodeMap["F16"] = 61443
        keyCodeMap["F17"] = 61444
        keyCodeMap["F18"] = 61445
        keyCodeMap["F19"] = 61446
        keyCodeMap["F20"] = 61447
        keyCodeMap["F21"] = 61448
        keyCodeMap["F22"] = 61449
        keyCodeMap["F23"] = 61450
        keyCodeMap["F24"] = 61451
        keyCodeMap["PRINTSCREEN"] = 154
        keyCodeMap["INSERT"] = 155
        keyCodeMap["HELP"] = 156
        keyCodeMap["META"] = 157
        keyCodeMap["WIN"] = 524
        keyCodeMap["CMD"] = 157
        keyCodeMap["BACK_QUOTE"] = 192
        keyCodeMap["QUOTE"] = 222
        keyCodeMap["KP_UP"] = 224
        keyCodeMap["KP_DOWN"] = 225
        keyCodeMap["KP_LEFT"] = 226
        keyCodeMap["KP_RIGHT"] = 227
        keyCodeMap["DEAD_GRAVE"] = 128
        keyCodeMap["DEAD_ACUTE"] = 129
        keyCodeMap["DEAD_CIRCUMFLEX"] = 130
        keyCodeMap["DEAD_TILDE"] = 131
        keyCodeMap["DEAD_MACRON"] = 132
        keyCodeMap["DEAD_BREVE"] = 133
        keyCodeMap["DEAD_ABOVEDOT"] = 134
        keyCodeMap["DEAD_DIAERESIS"] = 135
        keyCodeMap["DEAD_ABOVERING"] = 136
        keyCodeMap["DEAD_DOUBLEACUTE"] = 137
        keyCodeMap["DEAD_CARON"] = 138
        keyCodeMap["DEAD_CEDILLA"] = 139
        keyCodeMap["DEAD_OGONEK"] = 140
        keyCodeMap["DEAD_IOTA"] = 141
        keyCodeMap["DEAD_VOICED_SOUND"] = 142
        keyCodeMap["DEAD_SEMIVOICED_SOUND"] = 143
        keyCodeMap["AMPERSAND"] = 150
        keyCodeMap["ASTERISK"] = 151
        keyCodeMap["QUOTEDBL"] = 152
        keyCodeMap["LESS"] = 153
        keyCodeMap["GREATER"] = 160
        keyCodeMap["BRACELEFT"] = 161
        keyCodeMap["BRACERIGHT"] = 162
        keyCodeMap["AT"] = 512
        keyCodeMap["COLON"] = 513
        keyCodeMap["CIRCUMFLEX"] = 514
        keyCodeMap["DOLLAR"] = 515
        keyCodeMap["EURO_SIGN"] = 516
        keyCodeMap["EXCLAMATION_MARK"] = 517
        keyCodeMap["INVERTED_EXCLAMATION_MARK"] = 518
        keyCodeMap["LEFT_PARENTHESIS"] = 519
        keyCodeMap["NUMBER_SIGN"] = 520
        keyCodeMap["PLUS"] = 521
        keyCodeMap["RIGHT_PARENTHESIS"] = 522
        keyCodeMap["UNDERSCORE"] = 523
        keyCodeMap["CONTEXT_MENU"] = 525
        keyCodeMap["FINAL"] = 24
        keyCodeMap["CONVERT"] = 28
        keyCodeMap["NONCONVERT"] = 29
        keyCodeMap["ACCEPT"] = 30
        keyCodeMap["MODECHANGE"] = 31
    }
}