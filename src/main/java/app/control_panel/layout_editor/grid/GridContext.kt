package app.control_panel.layout_editor.grid

import model.component.CommandResolver
import model.parser.component.ComponentParser
import system.image.ImageResolver
import java.util.*

interface GridContext {
    val imageResolver : ImageResolver
    val componentParser : ComponentParser
    val commandResolver : CommandResolver
    val resourceBundle : ResourceBundle
}