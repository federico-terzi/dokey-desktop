package system.model

abstract class Window {
    val PID: Int
    val titleText: String
    val executablePath: String?
    val application : Application?

    constructor(PID: Int, titleText: String, executablePath: String?, application: Application?) {
        this.PID = PID
        this.titleText = titleText
        this.executablePath = executablePath
        this.application = application
    }

    /**
     * Should implement the logic behind focusing on a system.window.
     */
    abstract fun focusWindow()

}