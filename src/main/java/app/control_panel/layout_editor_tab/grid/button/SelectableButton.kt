package app.control_panel.layout_editor_tab.grid.button

import app.control_panel.layout_editor_tab.grid.GridContext
import javafx.scene.input.MouseButton
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

    var onDeselectAllRequest: (() -> Unit)? = null

    var onDoubleClicked: ((MouseEvent) -> Unit)? = null

    init {
        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) {
                    onDoubleClicked?.invoke(it)
                }else{
                    if (!it.isShiftDown) {
                        onDeselectAllRequest?.invoke()
                    }

                    selected = !selected
                }
            }else{
                if (!selected) {
                    if (!it.isShiftDown) {
                        onDeselectAllRequest?.invoke()
                    }

                    selected = true
                }
            }
        }
    }
}
