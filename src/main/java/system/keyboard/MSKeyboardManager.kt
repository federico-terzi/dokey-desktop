package system.keyboard

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.win32.W32APIOptions

class MSKeyboardManager : AbstractKeyboardManager() {
    companion object {
        /**
         * Check if the given key has to be patched on windows.
         */
        fun isPatchedKey(keyCode: Int) : Boolean {
            // Check if it is an Arrow Key
            return keyCode in 37..40
        }

        fun nativeKeyPress(keyCode: Int) {
            // Prepare input reference
            val input = WinUser.INPUT()

            input.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
            input.input.setType("ki") // Because setting INPUT_INPUT_KEYBOARD is not enough: https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
            input.input.ki.wScan = WinDef.WORD(0)
            input.input.ki.time = WinDef.DWORD(0)
            input.input.ki.dwExtraInfo = BaseTSD.ULONG_PTR(0)

            input.input.ki.wVk = WinDef.WORD(keyCode.toLong()) // 0x41
            input.input.ki.dwFlags = WinDef.DWORD(0)  // keydown

            User32.INSTANCE.SendInput(WinDef.DWORD(1), input.toArray(1) as Array<WinUser.INPUT>, input.size())
        }

        fun nativeKeyRelease(keyCode: Int) {
            // Prepare input reference
            val input = WinUser.INPUT()

            input.type = WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD.toLong())
            input.input.setType("ki") // Because setting INPUT_INPUT_KEYBOARD is not enough: https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
            input.input.ki.wScan = WinDef.WORD(0)
            input.input.ki.time = WinDef.DWORD(0)
            input.input.ki.dwExtraInfo = BaseTSD.ULONG_PTR(0)

            input.input.ki.wVk = WinDef.WORD(keyCode.toLong()) // 0x41
            input.input.ki.dwFlags = WinDef.DWORD(2)  // keyup

            User32.INSTANCE.SendInput(WinDef.DWORD(1), input.toArray(1) as Array<WinUser.INPUT>, input.size())
        }
    }

    override fun pressKey(keyCode: Int) {
        if (isPatchedKey(keyCode)) {
            nativeKeyPress(keyCode)
        }else{
            super.pressKey(keyCode)
        }
    }

    override fun releaseKey(keyCode: Int) {
        if (isPatchedKey(keyCode)) {
            nativeKeyRelease(keyCode)
        }else{
            super.releaseKey(keyCode)
        }
    }
}