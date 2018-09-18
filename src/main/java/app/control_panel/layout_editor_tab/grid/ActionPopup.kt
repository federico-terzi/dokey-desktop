package app.control_panel.layout_editor_tab.grid

import app.ui.control.ImageTitleDescriptionButton
import app.ui.popup.StyledPopup
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import system.image.ImageResolver

class ActionPopup(val imageResolver: ImageResolver) : StyledPopup() {
    private val addExistingCommandBtn = ImageTitleDescriptionButton(imageResolver, "asset:external-link", "Existing Command", "Choose an existing command...")  // TODO: i18n
    private val createCommandBtn = ImageTitleDescriptionButton(imageResolver, "asset:add_clean", "New Command", "Create a new command...")  // TODO: i18n

    var onNewCommandRequested : (() -> Unit)? = null
    var onExistingCommandRequested : (() -> Unit)? = null

    init {
        contentBox.alignment = Pos.CENTER
        contentBox.spacing = 5.0

        contentBox.children.addAll(addExistingCommandBtn, createCommandBtn)

        addExistingCommandBtn.maxWidth = Double.MAX_VALUE
        createCommandBtn.maxWidth = Double.MAX_VALUE

        addExistingCommandBtn.setOnAction {
            onExistingCommandRequested?.invoke()
        }
        createCommandBtn.setOnAction {
            onNewCommandRequested?.invoke()
        }
    }
}