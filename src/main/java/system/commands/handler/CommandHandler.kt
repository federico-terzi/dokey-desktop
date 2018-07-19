package system.commands.handler

import model.command.Command
import system.context.GeneralContext

abstract class CommandHandler<T: Command>(val context: GeneralContext) {
    fun handleCommand(command: Command) {
        command as T
        handleInternal(command)
    }

    protected abstract fun handleInternal(command: T)
}