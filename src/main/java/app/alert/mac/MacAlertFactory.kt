package app.alert.mac

import app.alert.AlertFactory
import app.alert.model.Alert
import app.alert.model.AlertOption
import app.bindings.JavaMacNativeUI
import com.sun.jna.Native

class MacAlertFactory : AlertFactory {
    override fun alert(title: String, text: String?, runOnJavaFxThread: Boolean) : Alert {
        return custom(title, text, listOf(AlertOption("OK")), isCritical = true, runOnJavaFxThread = runOnJavaFxThread)
    }

    override fun info(title: String, text: String?, runOnJavaFxThread: Boolean) : Alert {
        return custom(title, text, listOf(AlertOption("OK")), runOnJavaFxThread = runOnJavaFxThread)
    }

    override fun custom(title: String, text: String?, options: List<AlertOption>, isCritical: Boolean,
                        runOnJavaFxThread: Boolean) : Alert {
        val alert = MacAlert(title, text, options, isCritical, runOnJavaFxThread = runOnJavaFxThread)

        // Store the current alert to prevent garbage collection ( needed for the native callback )
        alertStore = alert

        return alert
    }

    override fun confirmation(title: String, text: String?, onYes: (() -> Unit)?, onNo: (() -> Unit)?, runOnJavaFxThread: Boolean): Alert {
        val yesOpt = AlertOption("Yes") { // TODO: i18n
            onYes?.invoke()
        }
        val noOpt = AlertOption("No") {  // TODO: i18n
            onNo?.invoke()
        }
        val cancelOpt = AlertOption("Cancel") {}  // TODO: i18n

        val alert = MacAlert(title, text, listOf(yesOpt, noOpt, cancelOpt), true, runOnJavaFxThread = runOnJavaFxThread)

        // Store the current alert to prevent garbage collection ( needed for the native callback )
        alertStore = alert

        return alert
    }

    companion object {
        // Used to store references to the current dialog, to prevent the garbage
        // collector from removing the objects and blocking the callback
        private var alertStore : MacAlert? = null
    }
}