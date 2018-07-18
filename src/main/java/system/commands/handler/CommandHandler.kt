package system.commands.handler

import model.command.Command
import system.DokeyContext

abstract class CommandHandler<T: Command>(val context: DokeyContext) {
    fun handleCommand(command: Command) {
        command as T
        handleInternal(command)
    }

    protected abstract fun handleInternal(command: T)
}