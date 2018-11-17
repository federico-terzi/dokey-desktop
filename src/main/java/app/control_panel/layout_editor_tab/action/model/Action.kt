package app.control_panel.layout_editor_tab.action.model

/**
 * This is the base interface used to implement the Command pattern, used in the editor to support
 * the Undo/Redo mechanism.
 * For more information: https://www.codeproject.com/Articles/33384/Multilevel-Undo-and-Redo-Implementation-in-Cshar-2
 */
interface Action {
    fun execute()
    fun unExecute()
}