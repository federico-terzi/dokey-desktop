package system.search.results

import model.command.Command
import system.context.SearchContext
import system.search.annotations.FilterableResult

@FilterableResult(filterName = "commands_category")
class CommandResult(context: SearchContext, val command: Command) : AbstractResult(context) {
    override val title: String
        get() = command.title!!
    override val description: String?
        get() = command.description
    override val extra: String?
        get() = command.quickCommand

    override val imageId: String?
        get() = command.iconId

    override fun executeAction() {
        context.commandEngine.execute(command)
    }

    override fun generateDragAndDropPayloadInternal(): String? {
        return "command:${command.id}"
    }
}