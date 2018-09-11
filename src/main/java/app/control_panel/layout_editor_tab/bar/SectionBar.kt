package app.control_panel.layout_editor_tab.bar

import app.control_panel.layout_editor_tab.bar.selectors.*
import javafx.event.EventHandler
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
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
    private val selectors = mutableListOf<Selector>()

    var onSectionClicked : ((Section) -> Unit)? = null

    init {
        this.styleClass.add("app_scroll_pane")

        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
        isPannable = true

        appBox.maxHeight = Double.MAX_VALUE
        appBox.maxWidth = Double.MAX_VALUE

        content = appBox

        // Setup the scrolling behaviour
        setOnScroll {
            if(Math.abs(it.deltaY) > 0) { // Scrolled vertically
                this.hvalue += it.deltaY / 400
            }
        }

        loadSections()
    }

    fun loadSections() {
        appBox.children.clear()

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
        this.selectors.clear()
        this.selectors.addAll(selectors)

        selectors.forEachIndexed { index, selector ->
            selector.initialize()
            appBox.children.add(selector)

            val changeAction : () -> Unit = {
                val previousSelected : Int = selectors.indexOfFirst { it.selected }

                // If the user clicks on the already selected one, do nothing
                if (previousSelected != index) {
                    onSelectorSelected(selector)
                }
            }

            selector.onAction = EventHandler {
                changeAction()
            }

            // Used when a user drag a file or url into another selector to focus the correct panel
            selector.onDragEntered = EventHandler {
                changeAction()
            }
        }

        // Automatically select the first one
        onSelectorSelected(selectors.first())
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

        val associatedSelector = selectors[index]
        onSelectorSelected(associatedSelector)
    }

    private fun onSelectorSelected(selector: Selector) {
        onSectionClicked?.invoke(selector.section)

        // Unselect previous selector
        selectors.filter { it != selector }.forEach { it.selected = false }

        // Select the current one
        selector.selected = true
    }
}