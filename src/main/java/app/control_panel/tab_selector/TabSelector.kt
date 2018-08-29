package app.control_panel.tab_selector

import javafx.scene.layout.Pane
import system.image.ImageResolver

const val TAB_OVERFLOW = 30.0

class TabSelector(val imageResolver: ImageResolver) : Pane() {
    private val tabs = mutableListOf<Tab>()

    init {
        tabs.add(Tab(imageResolver, "Devices", "asset:star"))
        tabs.add(Tab(imageResolver, "Devices1", "asset:star"))
        tabs.add(Tab(imageResolver, "Devices2", "asset:star"))
        tabs.add(Tab(imageResolver, "Devices3", "asset:star"))
        tabs.add(Tab(imageResolver, "Devices4", "asset:star"))

        tabs.forEach { tab ->
            this.children.add(tab)

            tab.styleClass.add("tab-selector-tab")

            tab.setOnAction {
                tab.toFront()
            }
        }

        tabs[0].styleClass.add("tab-left")
        tabs[tabs.size-1].styleClass.add("tab-right")
    }

    override fun layoutChildren() {
        super.layoutChildren()

        val tabWidth = this.width / tabs.size + TAB_OVERFLOW
        val tabSpacing = this.width / tabs.size

        tabs.forEachIndexed { i, tab ->
            tab.prefWidth = tabWidth
            tab.layoutX = tabSpacing * i
        }
    }
}