package system.commands.validator

import model.command.Command
import org.reflections.Reflections
import system.commands.validator.agent.ValidationAgent
import system.commands.validator.annotation.RegisterValidationAgent
import kotlin.reflect.KClass

class CommandValidator(val context: CommandValidationContext) {
    private val validationAgents = mutableMapOf<KClass<out Command>, ValidationAgent>()

    init {
        // Load validation agents using reflection
        val reflections = Reflections("system.commands.validator.agent")
        val agentClasses = reflections.getTypesAnnotatedWith(RegisterValidationAgent::class.java)
        agentClasses.forEach { agentClass ->
            val annotation = agentClass.getAnnotation(RegisterValidationAgent::class.java)
            val associatedType = annotation.commandType
            val agent = agentClass.getConstructor(CommandValidationContext::class.java).newInstance(context) as ValidationAgent
            validationAgents[associatedType] = agent
        }
    }

    fun validate(command: Command) : Boolean {
        val agent = validationAgents[command::class]

        if (agent != null) {
            // Analize the command
            if (!agent.analyze(command)) {
                // If not valid, try to fix it and return the result
                return agent.tryToFix(command)
            }
        }

        // If there are no agents registered for the specific command, it's valid by default
        return true
    }
}