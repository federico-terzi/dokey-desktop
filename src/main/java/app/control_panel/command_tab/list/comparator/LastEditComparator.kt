package app.control_panel.command_tab.list.comparator

import app.ui.control.SortingButton
import app.ui.model.Sorting
import model.command.Command
import java.util.Comparator

class LastEditComparator(val order: Sorting) : Comparator<Command>{
    override fun compare(o1: Command?, o2: Command?): Int {
        val lastEditOrdering = if (order == Sorting.ASCENDING) {
            o1!!.lastEdit!!.compareTo(o2!!.lastEdit!!)
        }else{
            o2!!.lastEdit!!.compareTo(o1!!.lastEdit!!)
        }
        if (lastEditOrdering == 0) {
            return o1.title!!.toLowerCase().compareTo(o2.title!!.toLowerCase())
        }else{
            return lastEditOrdering
        }
    }
}