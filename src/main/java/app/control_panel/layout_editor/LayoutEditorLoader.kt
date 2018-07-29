package app.control_panel.layout_editor

import app.control_panel.PanelLoader
import app.control_panel.layout_editor.grid.SectionGrid
import javafx.scene.Node
import model.parser.component.ComponentParser
import system.commands.CommandManager
import system.image.ImageResolver
import system.section.SectionManager
import java.util.*

class LayoutEditorLoader(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                         val componentParser: ComponentParser, val commandManager: CommandManager) : PanelLoader {
    override fun load(onLoadCompleted: (content: Node) -> Unit) {
        val sectionGrid = SectionGrid(sectionManager.getSections().first(), imageResolver, resourceBundle, componentParser, commandManager)

        onLoadCompleted(sectionGrid)
    }
}