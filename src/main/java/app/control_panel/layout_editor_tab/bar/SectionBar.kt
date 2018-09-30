package app.control_panel.layout_editor_tab.bar

import app.control_panel.layout_editor_tab.bar.selectors.*
import app.ui.control.StyledMenuItem
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import model.section.*
import system.image.ImageResolver
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.section.SectionManager
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.logging.Logger
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

const val DRAG_TIMEOUT = 500L // ms

class SectionBar(val sectionManager: SectionManager, override val applicationManager: ApplicationManager,
                 override val imageResolver: ImageResolver, override val commandManager: CommandManager) : ScrollPane(), SelectorContext {
    private val selectorTypes = mapOf<String, Class<out Selector>>(
            "launchpad" to LaunchpadSelector::class.java,
            "system" to SystemSelector::class.java,
            "app" to ApplicationSelector::class.java
    )

    private val appBox = HBox()
    private val selectors = mutableListOf<Selector>()

    // Used to delay section change during drag and drop
    private var dragTimer = Timer()

    // Currently selected selector
    var currentSelector : Selector? = null

    var onSectionClicked : ((Section) -> Unit)? = null

    // Context menu actions
    var onResetRequest : ((Section) -> Unit)? = null
    var onDeleteRequest : ((Section) -> Unit)? = null
    var onExportRequest : ((Section) -> Unit)? = null
    var onImportRequest: (() -> Unit)? = null

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

    fun loadSections(targetSection: Section? = null) {
        // Get the target section that should be selected,
        // if not specified, select the previous one if possible
        val _targetSection: Section? = targetSection ?: currentSelector?.section

        appBox.children.clear()

        val sections = sectionManager.getSections()
        val selectors = mutableListOf<Selector>()
        sections.forEach { section ->
            val selectorClass = selectorTypes[section.type]
            if (selectorClass != null) {
                try {
                    val selector = selectorClass.getConstructor(SelectorContext::class.java, SectionBar::class.java, Section::class.java)
                            .newInstance(this, this, section)
                    selectors.add(selector)
                }catch (ex: InvocationTargetException) {
                    LOG.severe("Error creating selector for section: $section: ${ex.targetException}")
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
                // Make sure the source isn't the button itself
                if (it.gestureSource != selector) {
                    dragTimer = Timer()
                    dragTimer.schedule(timerTask {
                        Platform.runLater {
                            changeAction()
                        }
                    }, DRAG_TIMEOUT)
                }
            }
            selector.setOnDragExited {
                dragTimer.cancel()
            }
        }

        if (_targetSection != null && selectors.any { it.section == _targetSection }) {
            val actualSelector = selectors.find { it.section == _targetSection }

            // Previous selector is still available, select it.
            onSelectorSelected(actualSelector!!)
        }else{
            // Automatically select the first one
            onSelectorSelected(selectors.first())
        }

    }

    fun selectSection(sectionId: String, canReloadSections: Boolean = false) {
        val associatedSelectorIndex = selectors.indexOfFirst { it.section.id == sectionId }
        if (associatedSelectorIndex >= 0 ) {
            selectSection(associatedSelectorIndex)
        }else{  // Section not found, maybe it was created lately and reloading them can solve the problem
            if (canReloadSections) {
                loadSections()
                selectSection(sectionId, canReloadSections = false)
            }
        }
    }
    fun selectSection(index: Int) {
        // Find the currently selected
        val currentSelector = selectors.indexOfFirst { selector -> selector.selected }

        val associatedSelector = selectors[index]

        // Scroll to the correct selector if needed
        val x = associatedSelector.boundsInParent.maxX
        val paneWidth = this.boundsInLocal.width
        val currentScroll = this.hvalue
        if (x < (currentScroll + associatedSelector.width) || x > (currentScroll+paneWidth)) {
            this.hvalue = x - paneWidth
        }

        onSelectorSelected(associatedSelector)
    }

    private fun onSelectorSelected(selector: Selector) {
        currentSelector = selector

        onSectionClicked?.invoke(selector.section)

        // Unselect previous selector
        selectors.filter { it != selector }.forEach { it.selected = false }

        // Select the current one
        selector.selected = true
    }

    companion object {
        val LOG = Logger.getGlobal()
    }
}