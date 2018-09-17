package app.control_panel.dialog.command_edit_dialog.builder

import system.applications.ApplicationManager
import system.image.ImageResolver

interface BuilderContext {
    val imageResolver : ImageResolver
    val applicationManager: ApplicationManager
}