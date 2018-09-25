package app.control_panel.layout_editor_tab.bar.selectors

import app.control_panel.layout_editor_tab.bar.SectionBar
import model.section.Section

class SystemSelector(context: SelectorContext, sectionBar: SectionBar, section: Section)
    : Selector(context, sectionBar, section, 1, isDeletable = false) {
    override val imageId: String = "asset:system"
}