package app.control_panel.layout_editor_tab.action.component

import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.action.model.SectionRelatedAction
import model.component.Component
import model.page.Page
import model.section.Section

class MoveComponentAction(val section: Section, val page: Page, val component: Component, val newX: Int, val newY: Int) : Action, SectionRelatedAction {
    override val relatedSections: List<Section> = listOf(section)

    private val initialX = component.x
    private val initialY = component.y

    override fun execute() {
        component.x = newX
        component.y = newY
    }

    override fun unExecute() {
        component.x = initialX
        component.y = initialY
    }
}