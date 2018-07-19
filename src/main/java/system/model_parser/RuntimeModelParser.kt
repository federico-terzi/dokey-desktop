package system.model_parser

import model.command.Command
import model.component.CommandResolver
import model.parser.DesktopModelParser
import org.reflections.Reflections
import system.commands.annotations.RegisterCommand

/**
 * Model Parser that automatically get registered commands using reflection
 */
class RuntimeModelParser(commandResolver: CommandResolver) : DesktopModelParser(commandResolver, getRegisteredCommands()) {
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