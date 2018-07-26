package system.applications.MS

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import system.ResourceUtils
import system.model.Application
import system.model.Window
import java.io.IOException

class MSWindow(titleText: String, application:Application?,
               val PID: Int, val hwnd: HWND)
    : Window(titleText, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() : Boolean{
        User32.INSTANCE.SetForegroundWindow(hwnd)

        // Restoring it if minimized
        val winplace = WinUser.WINDOWPLACEMENT()
        User32.INSTANCE.GetWindowPlacement(hwnd, winplace)
        if (winplace.showCmd == 2)  // MINIMIZED
            User32.INSTANCE.ShowWindow(hwnd, 9)  // Restore

        appActivate()

        return true
    }

    /**
     * Focus an application using the focusApplication vbscript
     */
    fun appActivate() {
        val runtime = Runtime.getRuntime()
        var scriptPath = ResourceUtils.getResource("/vbscripts/focusApplication.vbs").absolutePath

        // Remove the starting trailing slash
        if (scriptPath.startsWith("/")) {
            scriptPath = scriptPath.substring(1)
        }

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("cscript", "/nologo", scriptPath, PID.toString()))

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

