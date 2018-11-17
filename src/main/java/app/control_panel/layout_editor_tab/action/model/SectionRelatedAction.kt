package app.control_panel.layout_editor_tab.action.model

import model.section.Section

interface SectionRelatedAction : Action {
    val relatedSections: List<Section>
}