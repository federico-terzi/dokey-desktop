package app.control_panel.layout_editor_tab.action.model

/**
 * Group multiple actions together
 */
class MultipleAction(vararg actions: Action) : Action {
    private val actions : List<Action> = actions.toList()

    override fun execute() {
        this.actions.forEach { action ->
            action.execute()
        }
    }

    override fun unExecute() {
        this.actions.reversed().forEach { action ->
            action.unExecute()
        }
    }
}