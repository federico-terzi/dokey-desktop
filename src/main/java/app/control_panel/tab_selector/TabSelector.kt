package app.control_panel.tab_selector

import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import system.image.ImageResolver

const val TAB_OVERFLOW = 40.0
const val TAB_INITIAL_OFFSET = -20.0

// Unfortunately, due to a graphical glitch, this value cannot be dynamically generated
// that means that if the number of tab changes, this value must be manually recalculated.
// This is the formula
// tabWidth = this.width / tabs.size + TAB_OVERFLOW
const val TAB_WIDTH = 120.0

const val TAB_SELECTOR_HEIGHT = 60.0

class TabSelector(val imageResolver: ImageResolver) : Pane() {
    private val tabs = mutableListOf<Tab>()

    private var selectedTab : Tab? = null

    var onTabSelected : ((Int) -> Unit)? = null

    init {
        styleClass.add("tab-selector")

        tabs.add(Tab(imageResolver, "Devices", "asset:airplay"))
        tabs.add(Tab(imageResolver, "Panels", "asset:layout"))
        tabs.add(Tab(imageResolver, "Commands", "asset:command"))
        tabs.add(Tab(imageResolver, "Send", "asset:send"))
        tabs.add(Tab(imageResolver, "Settings", "asset:settings"))

        // Add all the tabs in reverse
        tabs.reversed().forEach { tab -> this.children.add(tab) }

        // Add all the listeners
        tabs.forEachIndexed {index, tab ->
            tab.setOnAction {
                selectTab(index)
            }
        }

        // Setup the clipping mask for the border
        val clippingMask = SVGPath()
        clippingMask.content = "M0,${TAB_SELECTOR_HEIGHT}l0.089-44.04C0.109,8.299,8.425,0,18.625,0h363c4.922,0,9.559,1.93,13.056,5.434c3.497,3.504,5.418,8.145,5.408,13.067l-0.089,${TAB_SELECTOR_HEIGHT-20}L0,${TAB_SELECTOR_HEIGHT}z"
        clippingMask.layoutX = 0.0
        clippingMask.layoutY = 0.0
        this.clip = clippingMask

        this.prefHeight = TAB_SELECTOR_HEIGHT

        // Initially select the first one
        selectedTab = tabs[0]
        Platform.runLater {
            renderSelection()
        }
    }

    fun selectTab(tabIndex: Int) {
        selectedTab = tabs[tabIndex]

        renderSelection()

        onTabSelected?.invoke(tabIndex)
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

        val tabSpacing = this.width / tabs.size

        tabs.forEachIndexed { i, tab ->
            tab.layoutX = TAB_INITIAL_OFFSET + tabSpacing * i
        }
    }
}