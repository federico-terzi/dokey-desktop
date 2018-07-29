package app.control_panel

import javafx.scene.Node

interface PanelLoader {
    fun load(onLoadCompleted: (content: Node) -> Unit)
}