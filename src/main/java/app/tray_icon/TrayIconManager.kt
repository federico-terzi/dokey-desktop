package app.tray_icon

interface TrayIconManager {
    var onTrayIconClicked : (() -> Unit)?

    var statusText : String
    var loading : Boolean

    val iconX : Int
    val iconY : Int

    fun initialize()

}