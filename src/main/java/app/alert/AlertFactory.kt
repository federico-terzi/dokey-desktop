package app.alert

import app.alert.mac.MacAlertFactory
import app.alert.model.Alert
import app.alert.model.AlertOption
import system.exceptions.UnsupportedOperatingSystemException
import utils.OSValidator

interface AlertFactory {
    fun alert(title: String, text: String?, runOnJavaFxThread: Boolean = true) : Alert
    fun info(title: String, text: String?, runOnJavaFxThread: Boolean = true) : Alert

    fun custom(title: String, text: String?, options: List<AlertOption>, isCritical: Boolean = false,
               runOnJavaFxThread: Boolean = true) : Alert

    companion object {
        val instance = loadInstance()

        private fun loadInstance() : AlertFactory {
            if (OSValidator.isMac()) {
                return MacAlertFactory()
            } //TODO: add windows

            throw UnsupportedOperatingSystemException("This OS is not supported")
        }
    }
}