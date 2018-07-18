package system.commands

import model.command.Command
import org.reflections.Reflections
import system.DokeyContext
import system.commands.annotations.RegisterHandler
import system.commands.handler.CommandHandler
import kotlin.reflect.KClass

class CommandEngine(val context: DokeyContext) {
    // This map will hold the association between the command types and the handlers
    // It is automatically populated at runtime using reflection analyzing the handlers
    // annotated with RegisterHandler
    private val handlerMap = mutableMapOf<KClass<out Command>, CommandHandler<out Command>>()

    init {
        // Load all the command handlers dynamically
        val reflections = Reflections("system.commands.handler")
        val handlers = reflections.getTypesAnnotatedWith(RegisterHandler::class.java)
        handlers.forEach {handlerClass ->
            val annotation = handlerClass.getAnnotation(RegisterHandler::class.java)
            annotation as RegisterHandler
            val handler = handlerClass.getConstructor(DokeyContext::class.java).newInstance(context) as CommandHandler<out Command>
            handlerMap[annotation.commandType] = handler
        }
    }

    fun execute(command: Command) {
        handlerMap[command::class]?.handleCommand(command)
    }
}