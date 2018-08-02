package app.control_panel.layout_editor.grid

import app.control_panel.layout_editor.GlobalKeyboardListener
import app.control_panel.layout_editor.model.ScreenOrientation
import javafx.animation.*
import javafx.scene.CacheHint
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import model.component.Component
import model.page.Page
import model.parser.component.ComponentParser
import model.section.Section
import javafx.util.Duration
import system.commands.CommandManager
import system.image.ImageResolver
import java.util.*


/**
 * This component will createView a Section into a Grid with button bar and animations.
 */
class SectionGrid(val section: Section,
                  val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                  val componentParser: ComponentParser, val commandManager: CommandManager,
                  val globalKeyboardListener: GlobalKeyboardListener)
    : VBox() {

    var onSectionModified : ((Section) -> Unit)? = null

    private var _screenOrientation : ScreenOrientation = ScreenOrientation.PORTRAIT

    var screenOrientation : ScreenOrientation
        get() = _screenOrientation
        set(value) {
            if (value == ScreenOrientation.PORTRAIT && _screenOrientation == ScreenOrientation.LANDSCAPE) {
                _screenOrientation = value
                rotate(90)
            } else if (value == ScreenOrientation.LANDSCAPE && _screenOrientation === ScreenOrientation.PORTRAIT) {
                _screenOrientation = value
                rotate(-90)
            }
        }

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

        componentGrids = HashMap<Tab, ComponentGrid>()

        // Add the pages
        for (page in section.pages!!) {
            // Create the page grid
            val grid = ComponentGrid(generateMatrix(page), screenOrientation, globalKeyboardListener,
                    resourceBundle, imageResolver, componentParser, commandManager)

            grid.onDeleteComponentRequest = { component ->
                page.components?.remove(component)
                onSectionModified?.invoke(section)
            }

            grid.onNewComponentRequest = {component ->
                page.components?.add(component)
                onSectionModified?.invoke(section)
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
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onAddTab() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        });
       /* // This listener is used by the tab pane controller to select/add tabs
        val onTabListener = object : TabPaneController.OnTabListener() {
            fun onTabSelected(index: Int) {
                tabPane.selectionModel.select(index)
                activePage = section.getPages().get(index)  // Update the active page
            }

            fun onAddTab() {
                sectionGridEventListener?.onRequestAddPage(section)
            }
        }*/

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

    /**
     * Delete all selected commands from all the grids
     */
    fun deleteSelected() {
        componentGrids.values.forEach { it.deleteSelected() }
    }

    private fun rotate(angle: Int) {
        val newView = createView()
        val oldContent = activePane

        oldContent!!.setCache(true)
        oldContent!!.setCacheHint(CacheHint.SPEED)

        val rotate = RotateTransition(
                Duration.seconds(ROTATE_SECTION_DURATION), oldContent)

        rotate.toAngle = angle.toDouble()
        rotate.setOnFinished { event -> render(newView) }

        val fadeOut = FadeTransition(
                Duration.seconds(ENTER_SECTION_FADE_DURATION), oldContent)
        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.5

        val fadeIn = FadeTransition(
                Duration.seconds(ENTER_SECTION_FADE_DURATION), newView)
        fadeIn.fromValue = 0.5
        fadeIn.toValue = 1.0

        SequentialTransition(ParallelTransition(rotate, fadeOut), fadeIn).play()
    }

    fun fadeIn() {
        activePane!!.setCache(true)
        activePane!!.setCacheHint(CacheHint.SPEED)
        val fadeIn = FadeTransition(
                Duration.seconds(0.4), activePane)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        fadeIn.setOnFinished { event -> activePane!!.setCache(false) }
        fadeIn.play()
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