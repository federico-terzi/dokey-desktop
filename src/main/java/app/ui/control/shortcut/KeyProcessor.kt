package app.ui.control.shortcut

import javafx.scene.input.KeyEvent
import system.exceptions.UnsupportedOperatingSystemException
import utils.OSValidator

/**
 * This class is used to pre-process the keys after receiving them, based on the OS.
 */
abstract class KeyProcessor {
    var onTextKeyPressed : ((key: String) -> Unit)? = null

    abstract fun requestTextKeyPressed(keyEvent: KeyEvent)

    companion object {
        fun getInstance() : KeyProcessor {
            if (OSValidator.isWindows()) {
                return MSKeyProcessor()
            }else if (OSValidator.isMac()) {
                return MACKeyProcessor()
            }else{
                throw UnsupportedOperatingSystemException("OS not supported")
            }
        }
    }
}