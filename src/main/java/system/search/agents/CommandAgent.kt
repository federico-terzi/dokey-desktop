package system.search.agents

import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.CommandResult
import system.search.results.Result

@RegisterAgent(priority = 50, resultClass = CommandResult::class)
class CommandAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean = true

    override fun getResults(query: String): List<out Result> {
        val commands = context.commandManager.searchCommands(query, MAX_RESULTS_FOR_AGENT)
        val commandResults = commands.map {
            CommandResult(context, it)
        }
        return commandResults
    }
}