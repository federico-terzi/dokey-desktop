package app.control_panel.layout_editor_tab.action.component

import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.action.model.SectionRelated
import model.component.Component
import model.page.Page
import model.section.Section

class DeleteComponentAction(override val section: Section, val page: Page, val component: Component) : Action, SectionRelated {
    override fun execute() {
        page.components?.remove(component)
    }

    override fun unExecute() {
        page.components?.add(component)
    }
}