package app.alert.win

import app.alert.model.Alert
import app.alert.model.AlertOption
import app.bindings.JavaWinNativeUI
import com.sun.jna.WString
import javafx.application.Platform

class WinComplexAlert(val title: String, val description: String?, val options: List<AlertOption>,
                      val isCritical: Boolean = false, val includeCancel: Boolean = true, val useCommandLinks: Boolean = false,
                      val runOnJavaFxThread: Boolean = true) : Alert {

    private val callback: JavaWinNativeUI.DialogCallback = JavaWinNativeUI.DialogCallback {
        if (it >= 100) {  // Make sure everything went ok
            if (runOnJavaFxThread) {
                Platform.runLater {
                    // Call the correct options callback
                    options[it - 100].callback?.invoke()
                }
            } else {
                // Call the correct options callback
                options[it - 100].callback?.invoke()
            }
        }
    }

    override fun show() {
        Thread {
            showInternal()
        }.start()
    }

    override fun showAndWait() {
        showInternal()
    }

    private fun showInternal() {
        val buttons: Array<WString> = options.map { WString(it.text) }.toTypedArray()
        val isCriticalInt = if (isCritical) 1 else 0
        val includeCancelInt = if (includeCancel) 1 else 0
        val useCommandLinksInt = if (useCommandLinks) 1 else 0

        val titleWString = WString(title)
        val descWString = if (description == null) null else WString(description)

        JavaWinNativeUI.INSTANCE.displayDialog(titleWString, descWString, isCriticalInt, buttons, buttons.size, includeCancelInt,
                useCommandLinksInt, callback)
    }
}