package system.search.results

import model.command.Command
import system.context.SearchContext
import system.search.annotations.FilterableResult

@FilterableResult
class CommandResult(context: SearchContext, val command: Command) : AbstractResult(context) {
    override val title: String
        get() = command.title!!
    override val description: String?
        get() = command.description

    override val imageId: String?
        get() = command.iconId

    override fun executeAction() {
        context.commandEngine.execute(command)
    }


}