package app.control_panel.layout_editor_tab.grid

import app.control_panel.layout_editor_tab.action.ActionReceiver
import app.control_panel.layout_editor_tab.action.component.AddComponentAction
import app.control_panel.layout_editor_tab.action.component.DeleteComponentAction
import app.control_panel.layout_editor_tab.action.component.MultipleSectionRelatedAction
import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.model.ScreenOrientation
import app.ui.stage.BlurrableStage
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import model.component.Component
import model.page.Page
import model.parser.component.ComponentParser
import model.section.Section
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import system.applications.ApplicationManager
import java.util.*


/**
 * This component will createView a Section into a Grid with button bar and animations.
 */
class SectionGrid(val parent: BlurrableStage, val section: Section,
                  val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                  val componentParser: ComponentParser, val commandManager: CommandManager,
                  val applicationManager: ApplicationManager,
                  val dndCommandProcessor: DNDCommandProcessor,
                  val actionReceiver: ActionReceiver)
    : VBox(), ActionReceiver {

    var onSectionModified : ((Section) -> Unit)? = null
    var onRequestAddPage : ((Section) -> Unit)? = null

    private var activePage : Page? = null
    private var activePane : VBox? = null

    // This map will hold the component grid for each tab.
    private var componentGrids = HashMap<Tab, ComponentGrid>()

    init {
        invalidate()
    }

    /**
     * Trigger a re-rendering of the grid
     */
    fun invalidate() {
        this.children.clear()
        val view = createView()
        render(view)
    }

    /**
     * Render the view and display it.
     */
    private fun render(view: VBox) {
        this.children.clear()
        this.children.add(view)

        activePane = view

    }

    /**
     * Create the view with all the components into the container.
     */
    private fun createView(): VBox {
        // Create the tabpane for the pages and set it up
        val tabPane = TabPane()
        tabPane.styleClass.add("section-tab-pane")

        componentGrids = HashMap<Tab, ComponentGrid>()

        // Add the pages
        section.pages?.forEachIndexed { index, page ->
            // Create the page grid
            val grid = ComponentGrid(parent, generateMatrix(page), index, section.id!!, ScreenOrientation.PORTRAIT,
                    commandManager, applicationManager, dndCommandProcessor,
                    resourceBundle, imageResolver, componentParser, commandManager,
                    this)

            grid.onAddComponentsRequest = { components ->
                // Create a multiple action to add all the components at once
                val addActions = components.map { AddComponentAction(section, page, it) }
                val action = MultipleSectionRelatedAction(addActions)
                actionReceiver.notifyAction(action)
            }

            grid.onDeleteComponentsRequest = { components ->
                // Create a multiple action to delete all the components at once
                val deleteActions = components.map { DeleteComponentAction(section, page, it) }
                val action = MultipleSectionRelatedAction(deleteActions)
                actionReceiver.notifyAction(action)
            }



            // Create the tab and add the page grid
            val tab = Tab()
            tab.content = grid
            componentGrids[tab] = grid

            // Add the tab context menu
//            val contextMenu = ContextMenu()
//            val changeSize = MenuItem(resourceBundle.getString("change_grid_size"))
//            changeSize.setStyle("-fx-text-fill: black;")
//            changeSize.setOnAction(object : EventHandler<ActionEvent>() {
//                fun handle(event: ActionEvent) {
//                    sectionGridEventListener?.onRequestChangePageSize(page, section)
//                }
//            })
//            val moveLeft = MenuItem(resourceBundle.getString("move_left"))
//            moveLeft.setStyle("-fx-text-fill: black;")
//            moveLeft.setOnAction(object : EventHandler<ActionEvent>() {
//                fun handle(event: ActionEvent) {
//                    sectionGridEventListener?.onMovePageLeft(page, section)
//                }
//            })
//            val moveRight = MenuItem(resourceBundle.getString("move_right"))
//            moveRight.setStyle("-fx-text-fill: black;")
//            moveRight.setOnAction(object : EventHandler<ActionEvent>() {
//                fun handle(event: ActionEvent) {
//                    sectionGridEventListener?.onMovePageRight(page, section)
//                }
//            })
//            val delete = MenuItem(resourceBundle.getString("delete"))
//            delete.setStyle("-fx-text-fill: black;")
//            delete.setOnAction(object : EventHandler<ActionEvent>() {
//                fun handle(event: ActionEvent) {
//                    sectionGridEventListener?.onRequestDeletePage(page, section)
//                }
//            })
//            contextMenu.getItems().addAll(changeSize, SeparatorMenuItem(), moveLeft, moveRight,
//                    SeparatorMenuItem(), delete)
//            tab.contextMenu = contextMenu

            // Add the tab
            tabPane.tabs.add(tab)

            // Select active tab
            if (page == activePage) {
                tabPane.selectionModel.select(tab)
            }
        }

        val tabPaneController = TabPaneController(tabPane, componentGrids, object : TabPaneController.OnTabListener{
            override fun onTabSelected(index: Int) {
                tabPane.selectionModel.select(index)
                activePage = section.pages?.get(index)  // Update the active page
            }

            override fun onAddTab() {
                onRequestAddPage?.invoke(section)
            }

        })

        tabPane.setOnScroll {
            var scrolled = false
            if (it.deltaX > 1) {
                tabPaneController.selectTab(tabPane.selectionModel.selectedIndex-1)
                scrolled = true
            }else if (it.deltaX < -1) {
                tabPaneController.selectTab(tabPane.selectionModel.selectedIndex+1)
                scrolled = true
            }
            if (scrolled) {
                activePage = section.pages?.get(tabPane.selectionModel.selectedIndex)
                tabPaneController.render()
            }
        }

        val currentPane = VBox()
        currentPane.children.add(tabPane)
        currentPane.children.add(tabPaneController)

        // If the active page is contained in the current section, select the tab
        if (section.pages!!.contains(activePage)) {
            val tabIndex = section.pages!!.indexOf(activePage)
            tabPane.selectionModel.select(tabIndex)
        } else {  // Read the currently active page
            activePage = section.pages!!.get(tabPane.selectionModel.selectedIndex)
        }

        return currentPane
    }

    // Key Shortcuts
    val copyKeystrokeCombination = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
    val pasteKeystrokeCombination = KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)

    fun onKeyPress(event: KeyEvent) {
        if (event.code == KeyCode.BACK_SPACE || event.code == KeyCode.DELETE) {
            componentGrids.values.forEach { it.deleteSelected() }
        }else if (copyKeystrokeCombination.match(event)) {
            componentGrids.values.forEach { it.copySelected() }
        }else if (pasteKeystrokeCombination.match(event)) {

        }
    }

    override fun notifyAction(action: Action) {
        actionReceiver.notifyAction(action)
    }

    companion object {
        private val ENTER_SECTION_FADE_DURATION = 0.2
        private val ROTATE_SECTION_DURATION = 0.2

        private fun generateMatrix(page: Page): Array<Array<Component?>> {
            // Create the matrix
            val componentMatrix = Array<Array<Component?>>(page.colCount!!) { arrayOfNulls<Component>(page.rowCount!!) }

            // Add all the components
            for (component in page.components!!) {
                // Add the component to the matrix
                componentMatrix[component.x!!][component.y!!] = component
            }

            return componentMatrix
        }
    }
}