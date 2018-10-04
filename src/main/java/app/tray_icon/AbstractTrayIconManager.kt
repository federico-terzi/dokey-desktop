package app.tray_icon

import app.MainApp
import java.util.*

abstract class AbstractTrayIconManager(val resourceBundle: ResourceBundle) : TrayIconManager {
    override var onTrayIconClicked: (() -> Unit)? = null

    protected var _iconX : Int = 0
    protected var _iconY : Int = 0

    override val iconX: Int
        get() = _iconX
    override val iconY: Int
        get() = _iconY


    private var _statusText = resourceBundle.getString("initializing")
    override var statusText: String
        get() = _statusText
        set(value) {
            _statusText = value
            updateStatusTooltip("Dokey ${MainApp.DOKEY_VERSION}\n$_statusText")
        }

    private var _loading: Boolean = false
    override var loading: Boolean
        get() = _loading
        set(value) {
            _loading = value
            updateStatusIcon()
        }

    protected abstract fun updateStatusTooltip(text: String)
    protected abstract fun updateStatusIcon()
}