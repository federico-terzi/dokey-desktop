package system.keyboard

import java.util.*
import java.util.logging.Logger

abstract class AbstractKeyboardManager : KeyboardManager {
    companion object {
        val LOG = Logger.getGlobal()
    }

    override fun sendKeystroke(keyStroke: String) {
        // Convert the string keystroke into a list of keys
        val keyList = parseKeyList(keyStroke)

        // Call the native implementation keystroke generator
        sendKeystrokeInternal(keyList)
    }

    abstract fun sendKeystrokeInternal(keys: List<String>)

    private fun parseKeyList(keyStroke: String) : List<String> {
        val tokenizer = StringTokenizer(keyStroke, "+")
        val keyList = mutableListOf<String>()
        while (tokenizer.hasMoreTokens()) {
            val key = tokenizer.nextToken()

            // Add the key to the list
            // Convert the PLUS key to +
            // ( used because + is also used as separator ).
            if (key == "PLUS") {
                keyList.add("+")
            }else{
                keyList.add(key)
            }
        }
        return keyList
    }
}