package system.MAC

import system.ResourceUtils
import system.model.Application
import system.model.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MACWindow(titleText: String,
                application: Application?,
                val appIdentifier : String)
    : Window(titleText, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() : Boolean {
        val scriptPath = ResourceUtils.getResource("/applescripts/focusApplication.scpt").absolutePath
        val runtime = Runtime.getRuntime()

        try {
            // Execute the process
            val proc = runtime.exec(arrayOf("osascript", scriptPath, appIdentifier, titleText))

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