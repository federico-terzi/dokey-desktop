package system.commands.loader

import model.command.Command

interface CommandLoader {
    /**
     * Return the list of command templates compatible with the current system.
     */
    fun getTemplateCommands() : List<Command>
}