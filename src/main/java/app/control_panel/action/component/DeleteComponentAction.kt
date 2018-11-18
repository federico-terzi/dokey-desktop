package app.control_panel.action.component

import app.control_panel.action.model.Action
import app.control_panel.action.model.SectionRelatedAction
import model.component.Component
import model.page.Page
import model.section.Section

class DeleteComponentAction(val section: Section, val page: Page, val component: Component) : Action, SectionRelatedAction {
    override val relatedSections: List<Section> = listOf(section)

    override fun execute() {
        page.components?.remove(component)
    }

    override fun unExecute() {
        page.components?.add(component)
    }
}