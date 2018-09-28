package system.commands.validator.agent

import model.command.Command

interface ValidationAgent {
    /**
     * Check if the given command is compatible with the current system.
     * @return true if the command is compatible with the system, false otherwise.
     */
    fun analyze(command: Command) : Boolean

    /**
     * If a command was previously analyzed and resulted incompatible with the current system, this method
     * can try to fix the problem.
     * @return true if the command was successfully fixed, false otherwise.
     */
    fun tryToFix(command: Command) : Boolean
}