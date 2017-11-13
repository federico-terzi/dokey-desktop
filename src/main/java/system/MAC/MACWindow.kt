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

    }
}