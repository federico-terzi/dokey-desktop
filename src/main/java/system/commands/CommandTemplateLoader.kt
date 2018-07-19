package system.commands

import model.command.Command
import org.reflections.Reflections
import system.commands.annotations.RegisterLoader
import system.commands.loader.CommandLoader
import system.context.CommandTemplateContext

class CommandTemplateLoader(val context: CommandTemplateContext) {
    private val loaders = mutableListOf<CommandLoader>()

    init {
        // Load all the command loaders dynamically
        val reflections = Reflections("system.commands.loader")
        val loadersClasses = reflections.getTypesAnnotatedWith(RegisterLoader::class.java)
        loadersClasses.forEach { loaderClass ->
            val loader = loaderClass.getConstructor(CommandTemplateContext::class.java).newInstance(context)
            loaders.add(loader as CommandLoader)
        }
    }

    fun getTemplateCommands() : List<Command> {
        return loaders.flatMap { it.getTemplateCommands() }
    }
}