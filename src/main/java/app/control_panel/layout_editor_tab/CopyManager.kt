package app.control_panel.layout_editor_tab

import model.component.Component

/**
 * This class is used to manage Copy/Paste operation inside sections
 */
class CopyManager {
    private var _copiedComponents : List<Component> = mutableListOf()
    var clipboardComponents : List<Component>
        get() = _copiedComponents
        set(value) {_copiedComponents = value}

    fun hasElements() : Boolean = _copiedComponents.isNotEmpty()
}