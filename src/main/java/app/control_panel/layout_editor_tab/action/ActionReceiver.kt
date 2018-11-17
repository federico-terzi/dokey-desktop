package app.control_panel.layout_editor_tab.action

import app.control_panel.layout_editor_tab.action.model.Action

interface ActionReceiver {
    fun notifyAction(action: Action)
}