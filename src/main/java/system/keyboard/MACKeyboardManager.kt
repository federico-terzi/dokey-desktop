package system.keyboard

import system.keyboard.bindings.MacKeyboardLib

class MACKeyboardManager : AbstractKeyboardManager() {
    private var currentKeys : List<String>? = null

    val sendShortcutResponseCallback = MacKeyboardLib.SendShortcutResponseCallback { response ->
        // Make sure the shortcut was typed correctly
        if (response <= 0) {
            LOG.warning("KeyboardShortcutDecodeError: decoding keys: ${currentKeys?.joinToString("+")}")
        }
    }

    override fun sendKeystrokeInternal(keys: List<String>) {
        val keyArray = keys.toTypedArray()

        // Save the reference for the async response callback
        currentKeys = keys

        // Send the shortcut using the native implementation
        MacKeyboardLib.INSTANCE.sendShortcut(keyArray, keys.size, sendShortcutResponseCallback)
    }
}