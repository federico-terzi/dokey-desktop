package system.MS

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import system.model.Application
import system.model.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.swing.Icon

class MSWindow(titleText: String, application:Application?,
               val PID: Int, val hwnd: HWND)
    : Window(titleText, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() : Boolean{
        appActivate()

        User32.INSTANCE.SetForegroundWindow(hwnd)

        return true
    }

    /**
     * Focus an application using the focusApplication vbscript
     */
    fun appActivate() {
        val runtime = Runtime.getRuntime()
        var scriptPath = javaClass.getResource("/vbscripts/focusApplication.vbs").path

        // Remove the starting trailing slash
        if (scriptPath.startsWith("/")) {
            scriptPath = scriptPath.substring(1)
        }

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("cscript", "/nologo", scriptPath, PID.toString()))

            // Wait for the process to complete
            proc.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

