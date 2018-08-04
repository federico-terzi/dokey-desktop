package app.control_panel.layout_editor.bar

import app.control_panel.layout_editor.bar.selectors.*
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.scene.shape.Shape
import model.section.*
import system.image.ImageResolver
import system.applications.ApplicationManager
import system.section.SectionManager

class SectionBar(val sectionManager: SectionManager, override val applicationManager: ApplicationManager,
                 override val imageResolver: ImageResolver) : ScrollPane(), SelectorContext {
    private val selectorTypes = mapOf<Class<out Section>, Class<out Selector>>(
            LaunchpadSection::class.java to LaunchpadSelector::class.java,
            SystemSection::class.java to SystemSelector::class.java,
            DefaultApplicationSection::class.java to ApplicationSelector::class.java
    )

    private val appBox = HBox()
    private val selectors : List<Selector>

    var onSectionClicked : ((Section, direction: Int) -> Unit)? = null

    init {
        this.styleClass.add("app_scroll_pane")

        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
        isPannable = true

        appBox.maxHeight = Double.MAX_VALUE
        appBox.maxWidth = Double.MAX_VALUE

        content = appBox

        val sections = sectionManager.getSections()
        val selectors = mutableListOf<Selector>()
        sections.forEach { section ->
            val selectorClass = selectorTypes[section.javaClass]
            if (selectorClass != null) {
                try {
                    val selector = selectorClass.getConstructor(SelectorContext::class.java, Section::class.java)
                            .newInstance(this, section)
                    selectors.add(selector)
                }catch (ex: SelectorLoadingException) {
                    ex.printStackTrace()
                }
            }
        }
        selectors.sort()
        this.selectors = selectors

        selectors.forEachIndexed { index, selector ->
            selector.initialize()
            appBox.children.add(selector)

            selector.onAction = EventHandler {
                val previousSelected : Int = selectors.indexOfFirst { it.selected }

                // If the user clicks on the already selected one, do nothing
                if (previousSelected != index) {
                    val direction = calculateDirection(previousSelected, index)

                    onSelectorSelected(selector, direction)
                }
            }
        }

        // Automatically select the first one
        onSelectorSelected(selectors.first(), 1)
    }

    fun selectSection(section: Section) {
        val associatedSelectorIndex = selectors.indexOfFirst { it.section == section }
        if (associatedSelectorIndex >= 0 ) {
            selectSection(associatedSelectorIndex)
        }
    }

    fun selectSection(index: Int) {
        // Find the currently selected
        val currentSelector = selectors.indexOfFirst { selector -> selector.selected }
        val direction = calculateDirection(currentSelector, index)

        val associatedSelector = selectors[index]
        onSelectorSelected(associatedSelector, direction)
    }

    private fun onSelectorSelected(selector: Selector, direction: Int = 1) {
        onSectionClicked?.invoke(selector.section, direction)

        // Unselect previous selector
        selectors.filter { it != selector }.forEach { it.selected = false }

        // Select the current one
        selector.selected = true
    }

    private fun calculateDirection(previous: Int, current: Int) : Int {
        return if (previous >= 0) {
            if (previous > current) {
                1
            }else{
                -1
            }
        }else{
            1
        }
    }
}