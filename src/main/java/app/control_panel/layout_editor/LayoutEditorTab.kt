package app.control_panel.layout_editor

import app.control_panel.ControlPanelTab
import app.control_panel.layout_editor.grid.SectionGrid
import app.control_panel.layout_editor.bar.SectionBar
import javafx.animation.Interpolator
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.page.DefaultPage
import model.parser.component.ComponentParser
import model.section.Section
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import system.applications.ApplicationManager
import system.section.SectionManager
import java.util.*

const val MAX_PAGES = 6

class LayoutEditorTab(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                      val componentParser: ComponentParser, val commandManager: CommandManager,
                      val applicationManager: ApplicationManager, val globalKeyboardListener: GlobalKeyboardListener,
                      val dndCommandProcessor: DNDCommandProcessor) : ControlPanelTab() {
    val sectionBar: SectionBar
    var sectionGrid: SectionGrid? = null
    val sectionGridContainer: ScrollPane = ScrollPane()  // Used as a workaround to fix overflowing transitions

    init {
        // The section bar must be included in a box as a workaround to add the gradient background without
        // the focus border of javafx
        val sectionBarContainer = VBox()
        sectionBarContainer.styleClass.add("app_scroll_pane_container")
        sectionBar = SectionBar(sectionManager, applicationManager, imageResolver)
        sectionBarContainer.children.add(sectionBar)

        children.add(sectionBarContainer)

        sectionGridContainer.styleClass.add("grid_scroll_pane_container")
        sectionGridContainer.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        sectionGridContainer.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        sectionGridContainer.isFitToHeight = true
        sectionGridContainer.isFitToWidth = true
        children.add(sectionGridContainer)

        sectionBar.onSectionClicked = { section, direction ->
            loadSection(section, direction = direction)
        }
    }

    override fun onFocus() {
        // Select the first one
        sectionBar.selectSection(0)
    }

    override fun onGlobalKeyPress(event: KeyEvent) {
        if (event.code == KeyCode.BACK_SPACE || event.code == KeyCode.DELETE) {
            sectionGrid?.deleteSelected()
        }
    }

    private fun loadSection(section: Section, direction: Int = 1) {
        val oldGrid = sectionGrid

        // Create the section grid
        sectionGrid = SectionGrid(section, imageResolver, resourceBundle, componentParser, commandManager,
                globalKeyboardListener, dndCommandProcessor)
        sectionGrid!!.onSectionModified = { section ->
            sectionManager.saveSection(section)
        }
        sectionGrid!!.onRequestAddPage = { section ->
            // Make sure to not exceed the limit
            if (section.pages!!.size < MAX_PAGES) {
                val newPage = DefaultPage()
                newPage.components = mutableListOf()
                newPage.colCount = SectionManager.DEFAULT_PAGE_COLS
                newPage.rowCount = SectionManager.DEFAULT_PAGE_ROWS
                section.pages!!.add(newPage)

                sectionManager.saveSection(section)

                sectionGrid!!.invalidate()
            }
        }

        // Replace the old grid with the new one, transitioning if necessary
        if (oldGrid != null) {
            slideAnimation(oldGrid, sectionGrid!!, direction)
        } else {
            sectionGridContainer.content = sectionGrid
        }
    }

    private fun slideAnimation(oldGrid: SectionGrid, newGrid: SectionGrid, direction: Int = 1) {
        val SLIDE_DURATION = 0.1

        val fadeOut = TranslateTransition(
                Duration.seconds(SLIDE_DURATION), oldGrid)
        fadeOut.interpolator = Interpolator.EASE_IN
        fadeOut.byX = direction * oldGrid.width
        fadeOut.setOnFinished { event -> sectionGridContainer.content = newGrid }

        val fadeIn = TranslateTransition(
                Duration.seconds(SLIDE_DURATION), newGrid)
        fadeIn.fromX = -direction * oldGrid.width
        fadeIn.toX = 0.0

        val crossFade = SequentialTransition(
                fadeOut, fadeIn)
        crossFade.play()
    }
}