package app.control_panel.tab_selector

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import system.image.ImageResolver

class Tab(val imageResolver: ImageResolver, val tabLabel : String, val tabImage : String) : Button() {
    private var nameLabel : Label

    private var _selected = false
    var selected : Boolean
        get() = _selected
        set(value) {
            if (value) {
                styleClass.add("tab-selected")

                nameLabel.text = tabLabel
            }else{
                styleClass.clear()
                styleClass.add("tab-selector-tab")

                nameLabel.text = ""
            }

            _selected = value
        }

    init {
        styleClass.add("tab-selector-tab")

        val vBox = VBox()
        vBox.alignment = Pos.CENTER
        nameLabel = Label(tabLabel)
        val image = imageResolver.resolveImage(tabImage, 24)
        val imageView = ImageView(image)
        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        vBox.children.addAll(imageView, nameLabel)

        graphic = vBox
    }
}