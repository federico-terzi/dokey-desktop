package app.control_panel.command_tab.list.comparator

import app.ui.control.SortingButton
import app.ui.model.Sorting
import model.command.Command
import java.util.Comparator

class NameComparator(val order: Sorting) : Comparator<Command>{
    override fun compare(o1: Command?, o2: Command?): Int {
        if (order == Sorting.ASCENDING) {
            return o1!!.title!!.toLowerCase().compareTo(o2!!.title!!.toLowerCase())
        }else{
            return o2!!.title!!.toLowerCase().compareTo(o1!!.title!!.toLowerCase())
        }
    }
}