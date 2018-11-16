package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import javafx.scene.input.MouseEvent


open class SelectableButton(context : GridContext) : DragButton(context) {
    private var _selected = false

    var selected : Boolean
        get() = _selected
        set(value) {
            if (value) {
                styleClass.add("selected")
            } else {
                styleClass.remove("selected")
            }
            _selected = value
        }

    var onSelected: ((MouseEvent) -> Unit)? = null
    var onDoubleClicked: ((MouseEvent) -> Unit)? = null

    init {
        setOnMouseClicked {
            if (it.clickCount == 2) {
                onDoubleClicked?.invoke(it)
            }else{
                onSelected?.invoke(it)
                selected = !selected
            }
        }
    }
}
