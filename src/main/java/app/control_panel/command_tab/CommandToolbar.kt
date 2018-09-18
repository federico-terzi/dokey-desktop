package app.control_panel.command_tab

import app.control_panel.dialog.command_type_dialog.CommandTypeDialog
import app.ui.control.ExpandableSearchBar
import app.ui.control.IconButton
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import model.command.Command
import system.commands.CommandDescriptor
import system.image.ImageResolver

class CommandToolbar(val parent: BlurrableStage, val imageResolver: ImageResolver) : HBox() {
    private val searchBar = ExpandableSearchBar(imageResolver)
    private val filterLabel = Label("All")  // TODO: i18n
    private val filterButton = IconButton(imageResolver, "asset:filter", 18)

    var onSearchChanged : ((String) -> Unit)?
        get() = searchBar.onSearchChanged
        set(value) {
            searchBar.onSearchChanged = value
        }

    var currentFilter : CommandDescriptor? = null
    var onFilterUpdate : ((Class<out Command>?) -> Unit)? = null

    init {
        styleClass.add("command-tab-toolbar")

        alignment = Pos.CENTER_LEFT

        val spacerPane = Pane()
        HBox.setHgrow(spacerPane, Priority.ALWAYS)

        filterLabel.styleClass.add("command-tab-toolbar-filter-label")

        children.addAll(searchBar, spacerPane, filterLabel, filterButton)

        filterButton.setOnAction {
            val dialog = CommandTypeDialog(parent, imageResolver)
            dialog.onTypeSelected = {commandDescriptor ->
                currentFilter = commandDescriptor
                onFilterUpdate?.invoke(commandDescriptor?.associatedCommandClass)

                if (currentFilter != null) {
                    filterLabel.text = commandDescriptor?.title  // TODO: i18n
                }else{
                    filterLabel.text = "All"  // TODO: i18n
                }
            }
            dialog.showWithAnimation()
        }
    }
}