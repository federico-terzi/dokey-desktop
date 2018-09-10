package app.control_panel.command_tab.list

import javafx.collections.FXCollections
import javafx.scene.control.ListView
import model.command.Command
import system.image.ImageResolver

class CommandListView(val imageResolver: ImageResolver) : ListView<Command>() {
    init {
        styleClass.add("command-list-view")

        setCellFactory {
            CommandListCell(imageResolver)
        }
    }
}