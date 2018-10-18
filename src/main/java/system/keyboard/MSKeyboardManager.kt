package system.keyboard

import com.sun.jna.WString
import system.keyboard.bindings.WinKeyboardLib

class MSKeyboardManager : AbstractKeyboardManager() {
    override fun sendKeystrokeInternal(keys: List<String>) {
        // Convert the key list to an array of WString
        val wkeys = keys.map { key -> WString(key) }.toTypedArray()

        // Send the shortcut using the native implementation
        val result = WinKeyboardLib.INSTANCE.sendShortcut(wkeys, wkeys.size)

        // Make sure the shortcut was typed correctly
        if (result <= 0) {
            LOG.warning("SendShortcutError for keys: ${keys.joinToString("+")}")
        }
    }
}