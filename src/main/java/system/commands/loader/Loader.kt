package system.commands.loader

import model.command.Command

interface Loader {
    fun getTemplateCommands() : List<Command>
}