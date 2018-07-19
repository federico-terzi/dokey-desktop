package system.search.agents

import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.AbstractResult
import system.search.results.CommandResult

@RegisterAgent(priority = 10)
class CommandAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean = true

    override val resultClass: Class<out AbstractResult>
        get() = CommandResult::class.java

    override fun getResults(query: String): List<out AbstractResult> {
        val commands = context.commandManager.searchCommands(query, 6)
        val commandResults = commands.map {
            CommandResult(context, it)
        }
        return commandResults
    }
}