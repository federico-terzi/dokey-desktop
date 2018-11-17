package app.control_panel.layout_editor_tab.action.component

import app.control_panel.layout_editor_tab.action.model.MultipleAction
import app.control_panel.layout_editor_tab.action.model.SectionRelatedAction
import model.section.Section

class MultipleSectionRelatedAction(actions : List<SectionRelatedAction>) : MultipleAction(actions), SectionRelatedAction {
    // Find all the related sections
    override val relatedSections: List<Section> = actions.flatMap { it.relatedSections }.distinct()
}