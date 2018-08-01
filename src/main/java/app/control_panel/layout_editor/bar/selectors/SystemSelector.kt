package app.control_panel.layout_editor.bar.selectors

import model.section.Section

class SystemSelector(context: SelectorContext, section: Section) : Selector(context, section, 1) {
    override val imageId: String = "asset:system"
}