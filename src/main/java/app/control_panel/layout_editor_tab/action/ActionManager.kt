package app.control_panel.layout_editor_tab.action

import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.action.model.SectionRelatedAction
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

            print("undo $action")

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

            print("redo $action")

            if (action is SectionRelatedAction) {
                action.relatedSections.forEach { section ->
                    onSectionModified?.invoke(section)
                }
            }

            undoStack.push(action)
        }
    }
}