package app.alert.mac

import app.alert.model.Alert

import app.alert.model.AlertOption
import javafx.application.Platform
import system.ResourceUtils
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MacAlert(val library: JavaMacNativeUI, val title: String, val description: String?, val options: List<AlertOption>,
               val isCritical: Boolean = false, val runOnJavaFxThread : Boolean = true) : Alert {
    private val waitLock = ReentrantLock()
    private var isWakenUp = false

    private val callback : JavaMacNativeUI.DialogCallback = JavaMacNativeUI.DialogCallback {
        if (it > 0) {  // Make sure everything went ok
            if (runOnJavaFxThread) {
                Platform.runLater {
                    // Call the correct options callback
                    options[it-1].callback?.invoke()
                }
            }else{
                // Call the correct options callback
                options[it-1].callback?.invoke()
            }
        }

        waitLock.withLock {
            isWakenUp = true;
        }
    }

    override fun show() {
        val buttons : Array<String> = options.map { it.text }.toTypedArray()
        val isCriticalInt = if (isCritical) 1 else 0

        library.displayDialog(iconFile, title, description, buttons, buttons.size, isCriticalInt, callback)
    }

    override fun showAndWait() {
        show()

        while (true) {
            var shouldSleep = false
            waitLock.withLock {
                shouldSleep = !isWakenUp
            }

            if (shouldSleep) {
                Thread.sleep(200)
            }else{
                break
            }
        }

    }

    companion object {
        val iconFile = ResourceUtils.getResource("/assets/icon.png").absolutePath
    }
}