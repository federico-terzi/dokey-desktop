package app.control_panel.layout_editor_tab.bar.selectors

import app.control_panel.layout_editor_tab.bar.SectionBar
import model.section.Section

class LaunchpadSelector(context: SelectorContext, sectionBar: SectionBar, section: Section)
    : Selector(context, sectionBar, section, 0, isDeletable = false) {
    override val imageId: String = "asset:home"
}