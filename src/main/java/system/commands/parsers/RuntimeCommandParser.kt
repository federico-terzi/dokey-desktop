package system.commands.parsers

import model.command.Command
import model.parser.command.CategoryCommandParser
import org.reflections.Reflections
import system.commands.annotations.RegisterCommand

/**
 * Command Parser that automatically get registered commands using reflection
 */
class RuntimeCommandParser() : CategoryCommandParser(getRegisteredCommands()) {
    companion object {
        fun getRegisteredCommands() : List<Class<out Command>> {
            val commandList = mutableListOf<Class<out Command>>()

            // Load all the command handlers dynamically
            val reflections = Reflections("system.commands")
            val commands = reflections.getTypesAnnotatedWith(RegisterCommand::class.java)
            commands.forEach { commandClass ->
                commandList.add(commandClass as Class<out Command>)
            }

            return commandList
        }
    }
}