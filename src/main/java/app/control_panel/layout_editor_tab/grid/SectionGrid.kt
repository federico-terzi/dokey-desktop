package app.control_panel.layout_editor_tab.grid

import app.control_panel.layout_editor_tab.CopyManager
import app.control_panel.action.ActionReceiver
import app.control_panel.action.component.AddComponentAction
import app.control_panel.action.component.DeleteComponentAction
import app.control_panel.action.component.MoveComponentAction
import app.control_panel.action.component.MultipleSectionRelatedAction
import app.control_panel.action.model.Action
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
import system.section.SectionManager
import java.util.*


/**
 * This component will createView a Section into a Grid with button bar and animations.
 */
class SectionGrid(val parent: BlurrableStage, val section: Section,
                  val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                  val componentParser: ComponentParser, val commandManager: CommandManager,
                  val applicationManager: ApplicationManager,
                  val dndCommandProcessor: DNDCommandProcessor,
                  val actionReceiver: ActionReceiver, val sectionManager: SectionManager,
                  val copyManager: CopyManager)
    : VBox(), ActionReceiver {

    var onSectionModified : ((Section) -> Unit)? = null
    var onRequestAddPage : ((Section) -> Unit)? = null

    private var activePage : Page? = null
    private var activePane : VBox? = null

    // This map will hold the component grid for each tab.
    private var componentGrids = HashMap<Tab, ComponentGrid>()

    private var tabPane : TabPane = TabPane()

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
        tabPane = TabPane()
        tabPane.styleClass.add("section-tab-pane")

        componentGrids = HashMap<Tab, ComponentGrid>()

        // Add the pages
        section.pages?.forEachIndexed { index, page ->
            // Create the page grid
            val grid = ComponentGrid(parent, generateMatrix(page), index, section.id!!,
                    commandManager, applicationManager, dndCommandProcessor,
                    resourceBundle, imageResolver, componentParser, commandManager,
                    this, copyManager)

            grid.onRefreshRequest = {
                invalidate()
            }

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

            grid.onMoveComponentsRequest = { componentDragReference, newPositions ->
                // In order to move the components, we have to make the following actions
                // * Delete the component from the old section
                // * Move the component to the new coordinates
                // * Add the component to the new section

                // Obtain a reference to the old section and page instances
                val oldSection : Section? = if (componentDragReference.sectionId == section.id) {
                    section
                }else{
                    sectionManager.getSection(componentDragReference.sectionId)
                }
                val oldPage : Page? = oldSection?.pages?.get(componentDragReference.pageIndex)

                // Make sure they are valid
                if (oldSection != null && oldPage != null) {
                    // Create all the delete actions
                    val deleteActions = componentDragReference.components.map {
                        DeleteComponentAction(oldSection, oldPage, it)
                    }

                    // Create all the move actions
                    val moveActions = componentDragReference.components.mapIndexed { index, component ->
                        MoveComponentAction(oldSection, oldPage, component, newPositions[index].first, newPositions[index].second)
                    }

                    // Create all the add actions
                    val addActions = componentDragReference.components.map {
                        AddComponentAction(section, page, it)
                    }

                    // Join all those actions into a single one
                    val action = MultipleSectionRelatedAction(deleteActions + moveActions + addActions)
                    actionReceiver.notifyAction(action)
                }
            }

            grid.onSwapComponentsRequest = { componentDragReference, targetComponents ->
                // In order to swap the components, we have to make the following actions
                // * Delete the component from the old section
                // * Move the component to the coordinates of the target component
                // * Add the component to the target section
                // * Delete the old component from the target section
                // * Move the old component to the original position of the component
                // * Add the old component to the old section

                // Obtain a reference to the old section and page instances
                val oldSection : Section? = if (componentDragReference.sectionId == section.id) {
                    section
                }else{
                    sectionManager.getSection(componentDragReference.sectionId)
                }
                val oldPage : Page? = oldSection?.pages?.get(componentDragReference.pageIndex)

                // Make sure they are valid
                if (oldSection != null && oldPage != null) {
                    // Get the target positions for both the lists
                    val oldPositions = componentDragReference.components.map { Pair(it.x!!, it.y!!) }
                    val targetPositions = targetComponents.map { Pair(it.x!!, it.y!!) }

                    // MOVE THE NEW COMPONENTS

                    // Create all the delete actions
                    val newDeleteActions = componentDragReference.components.map {
                        DeleteComponentAction(oldSection, oldPage, it)
                    }

                    // Create all the move actions
                    val newMoveActions = componentDragReference.components.mapIndexed { index, component ->
                        MoveComponentAction(oldSection, oldPage, component, targetPositions[index].first, targetPositions[index].second)
                    }

                    // Create all the add actions
                    val newAddActions = componentDragReference.components.map {
                        AddComponentAction(section, page, it)
                    }

                    // MOVE THE OLD COMPONENTS

                    // Create all the delete actions
                    val oldDeleteActions = targetComponents.map {
                        DeleteComponentAction(section, page, it)
                    }

                    // Create all the move actions
                    val oldMoveActions = targetComponents.mapIndexed { index, component ->
                        MoveComponentAction(section, page, component, oldPositions[index].first, oldPositions[index].second)
                    }

                    // Create all the add actions
                    val oldAddActions = targetComponents.map {
                        AddComponentAction(oldSection, oldPage, it)
                    }

                    // Join all those actions into a single one
                    val action = MultipleSectionRelatedAction(newDeleteActions + newMoveActions + newAddActions +
                                                                      oldDeleteActions + oldMoveActions + oldAddActions)
                    actionReceiver.notifyAction(action)
                }
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
        // Get the currently selected grid
        val currentGrid = getCurrentComponentGrid()

        if (event.code == KeyCode.BACK_SPACE || event.code == KeyCode.DELETE) {
            currentGrid?.deleteSelected()
        }else if (copyKeystrokeCombination.match(event)) {
            currentGrid?.copySelected()
        }else if (pasteKeystrokeCombination.match(event)) {
            currentGrid?.pasteRequest()
        }
    }

    override fun notifyAction(action: Action) {
        actionReceiver.notifyAction(action)
    }

    private fun getCurrentComponentGrid() : ComponentGrid? {
        return tabPane.tabs[tabPane.selectionModel.selectedIndex].content as? ComponentGrid
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