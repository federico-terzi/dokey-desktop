package app.control_panel.layout_editor_tab.grid

import app.control_panel.layout_editor_tab.action.ActionManager
import app.control_panel.layout_editor_tab.action.ActionReceiver
import model.component.CommandResolver
import model.parser.component.ComponentParser
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import java.util.*

interface GridContext {
    val imageResolver : ImageResolver
    val componentParser : ComponentParser
    val commandResolver : CommandResolver
    val resourceBundle : ResourceBundle
    val dndCommandProcessor : DNDCommandProcessor
    val actionReceiver : ActionReceiver
}