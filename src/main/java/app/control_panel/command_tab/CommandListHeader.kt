package app.control_panel.command_tab

import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority

class CommandListHeader() : HBox() {
    private val nameLabel = Label("Name")  // TODO: change i18n
    private val dateLabel = Label("Date")  // TODO: change i18n

    init {
        styleClass.add("command-tab-list-header")
        prefHeight = 30.0

        val centralSpacePane = Pane()
        HBox.setHgrow(centralSpacePane, Priority.ALWAYS)

        children.addAll(nameLabel, centralSpacePane, dateLabel)

    }
}