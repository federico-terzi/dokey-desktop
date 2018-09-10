package app.control_panel.layout_editor_tab.dialog.app_select_dialog

import app.ui.control.StyledListView
import system.applications.Application
import system.image.ImageResolver

class ApplicationListView(val imageResolver: ImageResolver) : StyledListView<Application>() {
    init {
        styleClass.add("application-list-view")

        setCellFactory {
            ApplicationListCell(imageResolver)
        }
    }
}