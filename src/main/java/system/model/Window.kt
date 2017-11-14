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
     * Return true if succeeded.
     */
    abstract fun focusWindow() : Boolean

    override fun toString(): String {
        return "Window(PID=$PID, titleText='$titleText', executablePath=$executablePath, application=$application)"
    }
}