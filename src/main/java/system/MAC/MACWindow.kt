package system.MAC

import system.model.Application
import system.model.Window

class MACWindow(PID: Int, titleText: String,
                executablePath: String?, application: Application?)
    : Window(PID, titleText, executablePath, application) {

    /**
     * Focus on a system.window
     */
    override fun focusWindow() {
//        tell application "Terminal" to activate
//        tell application "Terminal" to set index of window 1 where name contains "federico" to 1

    }
}