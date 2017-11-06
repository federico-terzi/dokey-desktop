package window

import javax.swing.Icon

abstract class Window {
    val PID: Int
    val titleText: String
    val icon: Icon?
    val executablePath: String?

    constructor(PID: Int, titleText: String, icon: Icon?, executablePath: String?) {
        this.PID = PID
        this.titleText = titleText
        this.icon = icon
        this.executablePath = executablePath
    }

    /**
     * Should implement the logic behind focusing on a window.
     */
    abstract fun focusWindow();

}