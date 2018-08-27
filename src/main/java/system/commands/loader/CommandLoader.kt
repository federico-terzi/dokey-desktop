package system.commands.loader

import model.command.Command
import utils.OSValidator

interface CommandLoader {
    /**
     * Return the list of command templates compatible with the current system.
     */
    fun getTemplateCommands() : List<Command>

    companion object {
        fun getOSPathSuffix() : String {
            return when {
                OSValidator.isMac() -> "mac"
                OSValidator.isWindows() -> "win"
                else -> "error"
            }
        }
    }
}