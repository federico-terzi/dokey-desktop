package app.control_panel.layout_editor

import app.control_panel.PanelLoader
import javafx.scene.Node
import model.parser.component.ComponentParser
import system.commands.CommandManager
import system.image.ImageResolver
import system.model.ApplicationManager
import system.section.SectionManager
import java.util.*

class LayoutEditorLoader(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                         val componentParser: ComponentParser, val commandManager: CommandManager,
                         val applicationManager: ApplicationManager, val globalKeyboardListener: GlobalKeyboardListener) : PanelLoader {
    override fun load(onLoadCompleted: (content: Node) -> Unit) {
        val layoutEditorBox = LayoutEditorBox(sectionManager, imageResolver, resourceBundle, componentParser,
                commandManager, applicationManager, globalKeyboardListener)

        onLoadCompleted(layoutEditorBox)
    }
}