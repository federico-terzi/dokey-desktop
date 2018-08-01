package app.control_panel.layout_editor

import app.control_panel.layout_editor.grid.SectionGrid
import app.control_panel.layout_editor.bar.SectionBar
import javafx.animation.Interpolator
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.parser.component.ComponentParser
import model.section.Section
import system.commands.CommandManager
import system.image.ImageResolver
import system.model.ApplicationManager
import system.section.SectionManager
import java.util.*

class LayoutEditorBox(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                      val componentParser: ComponentParser, val commandManager: CommandManager, val applicationManager: ApplicationManager) : VBox() {
    val sectionBar : SectionBar
    var sectionGrid : SectionGrid? = null

    init {
        // The section bar must be included in a box as a workaround to add the gradient background without
        // the focus border of javafx
        val sectionBarContainer = VBox()
        sectionBarContainer.styleClass.add("app_scroll_pane_container")
        sectionBar = SectionBar(sectionManager, applicationManager, imageResolver)
        sectionBarContainer.children.add(sectionBar)

        children.add(sectionBarContainer)

        sectionBar.onSectionClicked = { section, direction ->
            loadSection(section, direction = direction)
        }

        // Select the first one
        sectionBar.selectSection(0)
    }

    private fun loadSection(section: Section, direction: Int = 1) {
        val oldGrid = sectionGrid

        // Add the section to the screen
        sectionGrid = SectionGrid(section, imageResolver, resourceBundle, componentParser, commandManager)

        // Remove the previous section if present
        if (oldGrid != null) {
            slideAnimation(oldGrid, sectionGrid!!, direction)
        }else{
            children.add(sectionGrid)
        }
    }

    private fun slideAnimation(oldGrid : SectionGrid, newGrid : SectionGrid, direction: Int = 1) {
        val SLIDE_DURATION = 0.1

        val fadeOut = TranslateTransition(
                Duration.seconds(SLIDE_DURATION), oldGrid)
        fadeOut.interpolator = Interpolator.EASE_IN
        fadeOut.byX = direction * oldGrid.width
        fadeOut.setOnFinished { event -> children.add(newGrid); children.remove(oldGrid) }

        val fadeIn = TranslateTransition(
                Duration.seconds(SLIDE_DURATION), newGrid)
        fadeIn.fromX = -direction * oldGrid.width
        fadeIn.toX = 0.0

        val crossFade = SequentialTransition(
                fadeOut, fadeIn)
        crossFade.play()
    }
}