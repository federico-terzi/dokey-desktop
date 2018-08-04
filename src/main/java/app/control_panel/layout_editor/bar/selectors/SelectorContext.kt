package app.control_panel.layout_editor.bar.selectors

import system.applications.ApplicationManager
import system.image.ImageResolver

interface SelectorContext {
    val applicationManager : ApplicationManager
    val imageResolver : ImageResolver
}