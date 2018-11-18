package app.control_panel.action.model

import model.section.Section

interface SectionRelatedAction : Action {
    val relatedSections: List<Section>
}