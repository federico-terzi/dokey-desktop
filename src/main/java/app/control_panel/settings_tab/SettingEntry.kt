package app.control_panel.settings_tab

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class SettingEntry(val title : String, val description: String, val settingControl: Node) : HBox() {
    private val titleLabel = Label(title)
    private val descriptionLabel = Label(description)

    init {
        styleClass.add("setting-entry")

        alignment = Pos.CENTER_LEFT

        titleLabel.styleClass.add("setting-entry-title")
        descriptionLabel.styleClass.add("setting-entry-desc")

        val vBox = VBox()
        vBox.styleClass.add("setting-entry-vbox")
        HBox.setHgrow(vBox, Priority.ALWAYS)
        vBox.prefWidth = 1.0
        vBox.minWidth = 0.0
        vBox.children.addAll(titleLabel, descriptionLabel)

        children.addAll(vBox, settingControl)
    }
}