package app.control_panel.action

import app.control_panel.action.model.Action
import app.control_panel.action.model.SectionRelatedAction
import model.section.Section
import java.util.*

class ActionManager {
    private val undoStack = Stack<Action>()
    private val redoStack = Stack<Action>()

    var onSectionModified : ((Section) -> Unit)? = null

    fun execute(action: Action) {
        action.execute()
        undoStack.push(action)
        redoStack.clear()  // Empty the redo stack
    }

    fun undo() {
        if (!undoStack.empty()) {
            val action = undoStack.pop()
            action.unExecute()

            println("undo $action")

            if (action is SectionRelatedAction) {
                action.relatedSections.forEach { section ->
                    onSectionModified?.invoke(section)
                }
            }

            redoStack.push(action)
        }
    }

    fun redo() {
        if (!redoStack.empty()) {
            val action = redoStack.pop()
            action.execute()

            println("redo $action")

            if (action is SectionRelatedAction) {
                action.relatedSections.forEach { section ->
                    onSectionModified?.invoke(section)
                }
            }

            undoStack.push(action)
        }
    }
}