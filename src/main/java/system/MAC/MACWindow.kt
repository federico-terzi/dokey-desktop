package system.MAC

import system.model.Application
import system.model.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MACWindow(PID: Int, titleText: String,
                executablePath: String?, application: Application?)
    : Window(PID, titleText, executablePath, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() : Boolean {
        val scriptPath = javaClass.getResource("/applescripts/focusApplication.scpt").path
        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("osascript", scriptPath, application?.name, titleText))

            // Get the output
            val br = BufferedReader(InputStreamReader(proc.errorStream))

            // Get the response
            val res : String = br.readLine()

            br.close()

            // Return the result
            return res == "OK" || res == "FALLBACK"

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }
}