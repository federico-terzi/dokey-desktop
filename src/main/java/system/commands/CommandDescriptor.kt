package system.commands

import model.command.Command

data class CommandDescriptor(val title : String, val iconId: String, val associatedCommandClass : Class<out Command>) {
    override fun toString(): String {
        return title
    }
}