package app.alert.win

import app.alert.model.Alert
import app.bindings.JavaWinNativeUI
import com.sun.jna.WString

class WinAlert(val title: String, val description: String?,
               val isCritical: Boolean = false, val runOnJavaFxThread: Boolean = true) : Alert {
    private val callback: JavaWinNativeUI.DialogCallback = JavaWinNativeUI.DialogCallback {
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
        val isCriticalInt = if (isCritical) 1 else 0

        val titleWString = WString(title)
        val descWString = if (description == null) null else WString(description)

        JavaWinNativeUI.INSTANCE.displayInfo(titleWString, descWString, isCriticalInt, callback)
    }
}