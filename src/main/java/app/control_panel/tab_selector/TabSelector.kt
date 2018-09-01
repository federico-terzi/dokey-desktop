package app.control_panel.tab_selector

import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import system.image.ImageResolver

const val TAB_OVERFLOW = 40.0
const val TAB_INITIAL_OFFSET = -20.0

const val TAB_SELECTOR_HEIGHT = 60.0

class TabSelector(val imageResolver: ImageResolver) : Pane() {
    private val tabs = mutableListOf<Tab>()

    private var selectedTab : Tab? = null

    var onTabSelected : ((Int) -> Unit)? = null

    init {
        tabs.add(Tab(imageResolver, "Devices", "asset:airplay"))
        tabs.add(Tab(imageResolver, "Layouts", "asset:layout"))
        tabs.add(Tab(imageResolver, "Commands", "asset:command"))
        tabs.add(Tab(imageResolver, "Send", "asset:send"))
        tabs.add(Tab(imageResolver, "Settings", "asset:settings"))

        tabs.forEachIndexed {index, tab ->
            this.children.add(tab)

            tab.setOnAction {
                selectedTab = tab

                renderSelection()

                onTabSelected?.invoke(index)
            }
        }

        // Setup the clipping mask for the border
        val clippingMask = SVGPath()
        clippingMask.content = "M0,${TAB_SELECTOR_HEIGHT}l0.089-44.04C0.109,8.299,8.425,0,18.625,0h315c4.922,0,9.559,1.93,13.056,5.434c3.497,3.504,5.418,8.145,5.408,13.067l-0.089,${TAB_SELECTOR_HEIGHT-20}L0,${TAB_SELECTOR_HEIGHT}z"
        clippingMask.layoutX = 0.0
        clippingMask.layoutY = 0.0
        this.clip = clippingMask

        this.prefHeight = TAB_SELECTOR_HEIGHT

        // Initially select the first one
        selectedTab = tabs[0]
        Platform.runLater {renderSelection()}
    }

    private fun renderSelection() {
        // Bring all the other tabs to back
        tabs.forEach { tab -> tab.toBack() }

        tabs.subList(0, tabs.indexOf(selectedTab)).reversed().forEach { it.toBack() }

        tabs.forEach { tab ->
            tab.selected = tab == selectedTab
        }

        selectedTab?.toFront()
    }

    override fun layoutChildren() {
        super.layoutChildren()

        val tabWidth = this.width / tabs.size + TAB_OVERFLOW
        val tabSpacing = this.width / tabs.size

        tabs.forEachIndexed { i, tab ->
            tab.prefWidth = tabWidth
            tab.layoutX = TAB_INITIAL_OFFSET + tabSpacing * i
        }
    }
}