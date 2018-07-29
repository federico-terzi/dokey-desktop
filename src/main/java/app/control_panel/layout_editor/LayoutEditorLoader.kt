package app.control_panel.layout_editor

import app.control_panel.PanelLoader
import app.control_panel.layout_editor.grid.SectionGrid
import javafx.scene.Node
import javafx.scene.layout.VBox
import model.section.Section

class LayoutEditorLoader(val sections : Array<Section>) : PanelLoader {
    override fun load(onLoadCompleted: (content: Node) -> Unit) {
        val sectionGrid = SectionGrid()
    }
}