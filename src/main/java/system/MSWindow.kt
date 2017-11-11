package system

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import javax.swing.Icon

class MSWindow(PID: Int, titleText: String, icon: Icon?,
               executablePath: String?, val hwnd: HWND)
    : Window(PID, titleText, icon, executablePath) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() {
        User32.INSTANCE.SetForegroundWindow(hwnd)
    }
}

