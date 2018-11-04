package app.control_panel.command_tab

import app.control_panel.command_tab.list.comparator.LastEditComparator
import app.control_panel.command_tab.list.comparator.NameComparator
import app.ui.control.SortingButton
import javafx.scene.CacheHint
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import model.command.Command
import system.image.ImageResolver
import java.util.Comparator

class CommandListHeader(val imageResolver: ImageResolver) : HBox() {
    private val nameSortButton: SortingButton
    private val dateSortButton: SortingButton

    var onSortingSelected : ((Comparator<Command>) -> Unit)? = null

    init {
        styleClass.add("command-tab-list-header")
        prefHeight = 30.0

        val centralSpacePane = Pane()
        HBox.setHgrow(centralSpacePane, Priority.ALWAYS)

        nameSortButton = SortingButton(imageResolver, "Name")  // TODO: i18n
        dateSortButton = SortingButton(imageResolver, "Date")  // TODO: i18n

        nameSortButton.onSortingSelected = { sorting ->
            dateSortButton.sortingEnabled = false  // Deselect the other one

            // Notify the new comparator
            onSortingSelected?.invoke(NameComparator(sorting))
        }
        dateSortButton.onSortingSelected = { sorting ->
            nameSortButton.sortingEnabled = false  // Deselect the other one

            // Notify the new comparator
            onSortingSelected?.invoke(LastEditComparator(sorting))
        }

        children.addAll(nameSortButton, centralSpacePane, dateSortButton)

        isCache = true
        cacheHint = CacheHint.SPEED
    }
}