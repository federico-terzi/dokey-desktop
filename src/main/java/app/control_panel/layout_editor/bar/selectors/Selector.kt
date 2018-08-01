package app.control_panel.layout_editor.bar.selectors

import model.section.Section

abstract class Selector(val context: SelectorContext, val section: Section, val priority : Int) : Comparable<Selector> {
    abstract val imageId : String

    var selected : Boolean = false

    override fun compareTo(other: Selector): Int {
        return priority.compareTo(other.priority)
    }
}