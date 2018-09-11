package app.control_panel.layout_editor_tab.bar.selectors

import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver

interface SelectorContext {
    val applicationManager : ApplicationManager
    val imageResolver : ImageResolver
    val commandManager: CommandManager
}