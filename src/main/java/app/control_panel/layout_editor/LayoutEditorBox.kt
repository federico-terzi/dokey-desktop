package app.control_panel.layout_editor

import app.control_panel.layout_editor.grid.SectionGrid
import app.control_panel.layout_editor.bar.SectionBar
import javafx.scene.layout.VBox
import model.parser.component.ComponentParser
import model.section.Section
import system.commands.CommandManager
import system.image.ImageResolver
import system.model.ApplicationManager
import system.section.SectionManager
import java.util.*

class LayoutEditorBox(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                      val componentParser: ComponentParser, val commandManager: CommandManager, val applicationManager: ApplicationManager) : VBox() {
    val sectionBar : SectionBar
    var sectionGrid : SectionGrid? = null

    init {
        // The section bar must be included in a box as a workaround to add the gradient background without
        // the focus border of javafx
        val sectionBarContainer = VBox()
        sectionBarContainer.styleClass.add("app_scroll_pane_container")
        sectionBar = SectionBar(sectionManager, applicationManager, imageResolver)
        sectionBarContainer.children.add(sectionBar)

        children.add(sectionBarContainer)

        sectionBar.onSectionClicked = {
            loadSection(it)
        }

        loadSection(sectionManager.getSections().first())
    }

    private fun loadSection(section: Section) {
        // Remove the previous section if present
        if (sectionGrid != null) {
            children.remove(sectionGrid)
        }

        // Add the section to the screen
        sectionGrid = SectionGrid(section, imageResolver, resourceBundle, componentParser, commandManager)
        children.add(sectionGrid)
    }
}