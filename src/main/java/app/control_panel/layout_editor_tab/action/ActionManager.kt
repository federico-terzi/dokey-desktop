package app.control_panel.layout_editor_tab.action

import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.action.model.SectionRelated
import model.section.Section
import java.util.*

class ActionManager {
    private val undoStack = Stack<Action>()
    private val redoStack = Stack<Action>()

    var onSectionModified : ((Section) -> Unit)? = null

    fun execute(action: Action) {
        action.execute()
        undoStack.push(action)
    }

    fun undo() {
        if (!undoStack.empty()) {
            val action = undoStack.pop()
            action.unExecute()

            if (action is SectionRelated) {
                onSectionModified?.invoke(action.section)
            }

            redoStack.push(action)
        }
    }

    fun redo() {
        if (!redoStack.empty()) {
            val action = redoStack.pop()
            action.execute()

            if (action is SectionRelated) {
                onSectionModified?.invoke(action.section)
            }

            undoStack.push(action)
        }
    }
}