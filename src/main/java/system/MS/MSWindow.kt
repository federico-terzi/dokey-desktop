package system.MS

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import system.model.Application
import system.model.Window
import javax.swing.Icon

class MSWindow(titleText: String,
               application:Application?, val hwnd: HWND)
    : Window(titleText, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() : Boolean{
        User32.INSTANCE.SetForegroundWindow(hwnd)

        return true
    }
}

