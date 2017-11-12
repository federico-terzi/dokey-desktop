package system.MS

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import system.model.Application
import system.model.Window
import javax.swing.Icon

class MSWindow(PID: Int, titleText: String,
               executablePath: String?, application:Application?, val hwnd: HWND)
    : Window(PID, titleText, executablePath, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() {
        User32.INSTANCE.SetForegroundWindow(hwnd)
    }
}
