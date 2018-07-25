package system.search.agents

import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.CommandResult
import system.search.results.Result

@RegisterAgent(priority = 10, resultClass = CommandResult::class)
class CommandAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean = true

    override fun getResults(query: String): List<out Result> {
        val commands = context.commandManager.searchCommands(query, 6)
        val commandResults = commands.map {
            CommandResult(context, it)
        }
        return commandResults
    }
}