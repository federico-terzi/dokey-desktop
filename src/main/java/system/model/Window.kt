package system.model

abstract class Window {
    val titleText: String
    val application : Application?

    constructor(titleText: String, application: Application?) {
        this.titleText = titleText
        this.application = application
    }

    /**
     * Should implement the logic behind focusing on a system.window.
     * Return true if succeeded.
     */
    abstract fun focusWindow() : Boolean

    override fun toString(): String {
        return "Window(titleText='$titleText', application=$application)"
    }
}