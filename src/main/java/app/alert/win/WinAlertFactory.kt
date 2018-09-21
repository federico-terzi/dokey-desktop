package app.alert.win

import app.alert.AlertFactory
import app.alert.model.Alert
import app.alert.model.AlertOption
import com.sun.jna.Native

class WinAlertFactory : AlertFactory {
    private val library = Native.loadLibrary<JavaWinNativeUI>("JavaWinNativeUI", JavaWinNativeUI::class.java)

    override fun alert(title: String, text: String?, runOnJavaFxThread: Boolean) : Alert {
        val alert = WinAlert(library, title, text, isCritical = true, runOnJavaFxThread = runOnJavaFxThread)

        // Store the current alert to prevent garbage collection ( needed for the native callback )
        alertStore = alert

        return alert
    }

    override fun info(title: String, text: String?, runOnJavaFxThread: Boolean) : Alert {
        val alert = WinAlert(library, title, text, isCritical = false, runOnJavaFxThread = runOnJavaFxThread)

        // Store the current alert to prevent garbage collection ( needed for the native callback )
        alertStore = alert

        return alert
    }

    override fun custom(title: String, text: String?, options: List<AlertOption>, isCritical: Boolean,
                        runOnJavaFxThread: Boolean) : Alert {
        val alert = WinComplexAlert(library, title, text, options, isCritical, runOnJavaFxThread = runOnJavaFxThread)

        // Store the current alert to prevent garbage collection ( needed for the native callback )
        alertStore = alert

        return alert
    }

    companion object {
        // Used to store references to the current dialog, to prevent the garbage
        // collector from removing the objects and blocking the callback
        private var alertStore : Alert? = null
    }
}