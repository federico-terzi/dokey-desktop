package app.control_panel.layout_editor_tab.action.model

/**
 * Group multiple actions together
 */
open class MultipleAction(val actions: List<Action>) : Action {
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