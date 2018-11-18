package app.control_panel.action

import app.control_panel.action.model.Action

interface ActionReceiver {
    fun notifyAction(action: Action)
}