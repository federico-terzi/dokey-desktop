package app.control_panel.command_tab.list

import model.command.Command

interface CommandActionListener {
    val onEditRequest : ((Command) -> Unit)?
    val onDeleteRequest : ((List<Command>) -> Unit)?
}