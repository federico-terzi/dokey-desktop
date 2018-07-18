package system.commands.handler

import model.command.Command

abstract class CommandHandler<T: Command>(val context: CommandContext) {
    fun handleCommand(command: Command) {
        command as T
        handleInternal(command)
    }

    abstract fun handleInternal(command: T)
}