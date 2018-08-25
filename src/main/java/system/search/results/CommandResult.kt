package system.search.results

import model.command.Command
import system.commands.general.AppRelatedCommand
import system.context.SearchContext
import system.search.annotations.FilterableResult

@FilterableResult
class CommandResult(context: SearchContext, val command: Command, val priority: Int = 50) : AbstractResult(context) {
    override val title: String
        get() = command.title!!
    override val description: String?
        get() = command.description
    override val extra: String?
        get() = command.quickCommand

    override val imageId: String?
        get() = command.iconId

    override val category: ResultCategory
        get() = if (command is AppRelatedCommand && command.app != null) {
            ResultCategory(context.applicationManager
                    .getApplication(command.app)?.name ?: "commands_category", priority)
        }else{
            ResultCategory(context.resourceBundle.getString("commands_category"), priority)
        }

    override fun executeAction() {
        context.commandEngine.execute(command)
    }

    override fun generateDragAndDropPayloadInternal(): String? {
        return "command:${command.id}"
    }
}