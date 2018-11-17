package app.control_panel.layout_editor_tab.grid

import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.control_panel.layout_editor_tab.action.ActionReceiver
import app.control_panel.layout_editor_tab.grid.button.ComponentButton
import app.control_panel.layout_editor_tab.grid.button.DragButton
import app.control_panel.layout_editor_tab.grid.button.EmptyButton
import app.control_panel.layout_editor_tab.grid.button.SelectableButton
import app.control_panel.layout_editor_tab.grid.dnd.ComponentDragReference
import app.control_panel.layout_editor_tab.grid.exception.OutOfMatrixBoundsException
import app.control_panel.layout_editor_tab.model.ScreenOrientation
import app.ui.stage.BlurrableStage
import javafx.geometry.HPos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import model.component.CommandResolver
import model.component.Component
import model.component.RuntimeComponent
import model.parser.component.ComponentParser
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import java.util.*


class ComponentGrid(val parent: BlurrableStage, val componentMatrix: Array<Array<Component?>>,
                    val pageIndex: Int, val sectionId: String,
                    val commandManager: CommandManager, val applicationManager: ApplicationManager,
                    override val dndCommandProcessor: DNDCommandProcessor,
                    override val resourceBundle: ResourceBundle,
                    override val imageResolver: ImageResolver, override val componentParser: ComponentParser,
                    override val commandResolver: CommandResolver,
                    override val actionReceiver: ActionReceiver) : GridPane(), GridContext {

    var onAddComponentsRequest: ((List<Component>) -> Unit)? = null

    // Listener used to delete a list of components
    var onDeleteComponentsRequest : ((List<Component>) -> Unit)? = null

    // Listener used to update the position of a component list. The second Pair list represents the new coordinates
    // for each component
    var onMoveComponentsRequest: ((ComponentDragReference, List<Pair<Int, Int>>) -> Unit)? = null

    // Listener used to swap the position of every component in the two lists
    var onSwapComponentsRequest: ((ComponentDragReference, List<Component>) -> Unit)? = null

    private val rowCount: Int = componentMatrix.size
    private val colCount: Int = componentMatrix[0].size

    // The list of selected components in the current grid
    private val selectedComponents : List<Component>
        get() = this.children.filter { it is ComponentButton && it.selected }.map {
                    it as ComponentButton
                    it.associatedComponent
                }

    // Indicate whether the user is drag and dropping components or not ( For example when moving )
    private var isDragAndDropping = false

    init {
        render()

        setupConstraints()

        this.styleClass.add("component-grid")

        // When the background of the grid is clicked, unselect all buttons
        setOnMouseClicked {
            unselectAllButtons()
        }
    }

    /**
     * Setup the GridPane constraint to have equally large buttons
     */
    private fun setupConstraints() {
        for (rowIndex in 0 until rowCount) {
            val rc = RowConstraints()
            rc.vgrow = Priority.ALWAYS // allow row to grow
            rc.isFillHeight = true // ask nodes to fill height for row
            rc.percentHeight = 100.0
            // other settings as needed...
            rowConstraints.add(rc)
        }
        for (colIndex in 0 until colCount) {
            val cc = ColumnConstraints()
            cc.hgrow = Priority.ALWAYS // allow column to grow
            cc.isFillWidth = true // ask nodes to fill space for column
            cc.percentWidth = 100.0
            // other settings as needed...
            columnConstraints.add(cc)
        }
    }

    /**
     * Render the componentMatrix into buttons in the GridPane
     */
    fun render() {
        // Delete all the previous nodes
        children.clear()

        // Add all the components
        for (col in 0 until colCount) {
            for (row in 0 until rowCount) {
                if (componentMatrix[col][row] != null) {
                    addComponentToGridPane(componentMatrix[col][row])
                } else {
                    addEmptyButtonToGridPane(col, row)
                }
            }
        }
    }

    /**
     * Add an empty button in the given position.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     */
    private fun addEmptyButtonToGridPane(col: Int, row: Int) : EmptyButton? {
        val emptyButton = EmptyButton(this)

        /*
        emptyButton.setOnMouseClicked{
            val popup = ActionPopup(imageResolver)

            popup.onExistingCommandRequested = {
                val dialog = CommandSelectDialog(parent, imageResolver, commandManager)
                dialog.onCommandSelected = { command ->
                    // Create a component with the given command
                    val component = RuntimeComponent(commandManager)
                    component.x = col
                    component.y = row
                    component.commandId = command.id

                    componentMatrix[col][row] = component

                    onNewComponentRequest?.invoke(component)

                    render()
                }
                dialog.showWithAnimation()
            }
            popup.onNewCommandRequested =  {
                val dialog = CommandEditDialog(parent, imageResolver, applicationManager, commandManager)
                dialog.onCommandSaved = {command ->
                    // Create a component with the given command
                    val component = RuntimeComponent(commandManager)
                    component.x = col
                    component.y = row
                    component.commandId = command.id

                    componentMatrix[col][row] = component

                    onNewComponentRequest?.invoke(component)

                    render()
                }
                dialog.showWithAnimation()
            }

            popup.showForComponent(parent, emptyButton)
            //popup.showUnderMouse(parent, it.screenX, it.screenY)
        }
        */

        addButtonToGridPane(col, row, emptyButton)

        return emptyButton
    }

    /**
     * Add a component to the GridPane.
     *
     * @param component the component to add.
     * @return
     */
    fun addComponentToGridPane(component: Component?) : SelectableButton? {
        // Make sure the component is not null
        if (component == null)
            return null

        // Extract the coordinates from the component
        val col = component!!.x!!
        val row = component!!.y!!

        // Get the current button
        val current = ComponentButton(this, component)

        current.onComponentActionListener = object : ComponentButton.OnComponentActionListener {
            override fun onComponentEdit() {
                if (component.commandId == null) {
                    return
                }

                val command = commandManager.getCommand(component.commandId!!) ?: return

                val dialog = CommandEditDialog(parent, imageResolver, applicationManager, commandManager)
                dialog.loadCommand(command)
                dialog.onCommandSaved = {
                    render()
                }
                dialog.showWithAnimation()
            }

            override fun onComponentDelete() {
                onDeleteComponentsRequest?.invoke(listOf(component))
            }

            // When the component is dropped away, request the
            // deletion from the grid
            override fun onComponentDroppedAway() {
//                deleteComponent(component!!, true)
//                render()
                // TODO
            }

        }

        current.onDoubleClicked = {
            current.onComponentActionListener?.onComponentEdit()
        }

        current.requestSelectedComponentReference = {
            val reference = ComponentDragReference(selectedComponents, pageIndex, sectionId,
                    col, row)
            reference
        }

        addButtonToGridPane(col, row, current)

        return current
    }

    /**
     * Add the given button to the GridPane, correcting the coordinates based on the screen orientation.
     * and adding the drag and drop listener.
     *
     * @param col    the col index in the component matrix.
     * @param row    the row index in the component matrix.
     * @param button the button to add.
     */
    private fun addButtonToGridPane(col: Int, row: Int, button: SelectableButton) {
        // Set up the drag and drop
        button.onComponentsDropped = fun (componentReference: ComponentDragReference) : Boolean {
            // Generate the mask for the given components
            val mask = generateSelectionMask(componentReference.components, componentReference.dragX, componentReference.dragY )

            try {
                // Get the target positions, and check if the bounds are exceeded
                val targetCoordinates = generateTargetCoordinates(mask, col, row)

                // Get the conflicting components
                val conflicts = getMaskConflicts(targetCoordinates)

                if (conflicts.size == (componentReference.components.intersect(conflicts)).size) {  // MOVE
                    // Explanation:
                    // The user is moving the elements if the number of conflicts is equal to the size of the
                    // intersection between the components and the conflicts.
                    // Think about dragging two horizontal elements right by one position, they are overlapping.

                    // Notify the listener
                    onMoveComponentsRequest?.invoke(componentReference, targetCoordinates)

                    return true
                }else if (conflicts.size == componentReference.components.size) {  // SWAP
                    // Notify the listener
                    onSwapComponentsRequest?.invoke(componentReference, conflicts)

                    return true
                }

                return false
            }catch (e: OutOfMatrixBoundsException) {
                return false
            }
        }

        button.onComponentsDragEntered = {
            isDragAndDropping = true
            renderDragAndDropDestinations(it, col, row)
        }

        button.onComponentsDragExited = {
            isDragAndDropping = false
            resetDragAndDropDestinations()
        }

        button.onExternalResourceDropped = { newCommand ->
            // Create a component with the given command
            val component = RuntimeComponent(commandResolver)
            component.x = col
            component.y = row
            component.commandId = newCommand.id

            // Notify the listener
            onAddComponentsRequest?.invoke(listOf(component))

            true
        }

        button.onDeselectAllRequest = {
            unselectAllButtons()
        }

        button.gridX = col
        button.gridY = row

        // Add the component to the grid
        this.add(button, col, row, 1, 1)
        GridPane.setHalignment(button, HPos.CENTER)
    }

    /**
     * This method is used to generate a list of translated pairs, corresponding to the coordinates
     * of the given components. For example, imagine that two components are selected:
     * * Component 1 ( x = 1, y = 0 )
     * * Component 2 ( x = 1, y = 1 )
     * And the dragging started on the (1, 0) component
     * This function will output this list
     * [(0, 0), (0, 1)]
     */
    private fun generateSelectionMask(components: List<Component>, x: Int, y: Int) : List<Pair<Int, Int>> {
        if (components.isEmpty()) {
            return emptyList()
        }

        // Convert the selected coordinates into a mask
        val coordinates = components.map { Pair(it.x!!, it.y!!) }

        // Translate the coordinates to the origin
        val translatedCoordinates = coordinates.map { Pair(it.first - x, it.second - y) }

        return translatedCoordinates
    }

    /**
     * Return a translated mask based on the given coordinates
     */
    private fun generateTargetCoordinates(mask: List<Pair<Int, Int>>, x: Int, y: Int) : List<Pair<Int, Int>> {
        // Generate the translated coordinates
        val coordinates = mask.map { Pair(it.first + x, it.second + y) }

        // Find the minimum and maximums
        val minX : Int = coordinates.map { it.first }.min()!!
        val minY : Int = coordinates.map { it.second }.min()!!
        val maxX : Int = coordinates.map { it.first }.max()!!
        val maxY : Int = coordinates.map { it.second }.max()!!

        // Check that the bounds of the mask do not exit the matrix
        if (minX < 0) {
            throw OutOfMatrixBoundsException()
        }else if (minY < 0) {
            throw OutOfMatrixBoundsException()
        }else if (maxX >= colCount) {
            throw OutOfMatrixBoundsException()
        }else if (maxY >= rowCount) {
            throw OutOfMatrixBoundsException()
        }

        return coordinates
    }

    /**
     * Return the list of the components that are overlapping with the current drop mask
     */
    private fun getMaskConflicts(targetCoordinates: List<Pair<Int, Int>>) : List<Component> {
        // Check which components are overridden by the mask
        val overlapping = targetCoordinates.map {
            componentMatrix[it.first][it.second]
        }.filterNotNull()

        return overlapping
    }

    private fun resetDragAndDropDestinations() {
        // Reset all the drag destinations
        this.children.filter { it is DragButton }.forEach {
            it as DragButton
            it.dragDestination = false
            it.dragError = false
        }
    }

    private fun renderDragAndDropDestinations(componentReference: ComponentDragReference, x: Int, y: Int) {
        // Find all the cells that will be marked as targets
        val mask = generateSelectionMask(componentReference.components, componentReference.dragX, componentReference.dragY )

        var errorTargets = listOf<Pair<Int, Int>>()

        try {
            // Get the target positions, and check if the bounds are exceeded
            val targetCoordinates = generateTargetCoordinates(mask, x, y)

            // Get the conflicting components
            val conflicts = getMaskConflicts(targetCoordinates)

            if (conflicts.size == (componentReference.components.intersect(conflicts)).size ||
                    conflicts.size == componentReference.components.size) {  // MOVE OR SWAP
                this.children.filter { it is DragButton }.forEach {
                    it as DragButton
                    if (Pair(it.gridX, it.gridY) in targetCoordinates) {
                        it.dragDestination = true
                    }
                }
                return
            }

            errorTargets = targetCoordinates
        }catch (e: OutOfMatrixBoundsException) {
            errorTargets = listOf(Pair(x, y))
        }

        this.children.filter { it is DragButton }.forEach {
            it as DragButton
            if (Pair(it.gridX, it.gridY) in errorTargets) {
                it.dragError = true
            }
        }
    }

    private fun unselectAllButtons() {
        this.children.filter { it is SelectableButton }.forEach {
            it as SelectableButton
            it.selected = false
        }
    }

    fun deleteSelected() {
        onDeleteComponentsRequest?.invoke(selectedComponents)
    }

    fun copySelected() {
        // Find all the selected buttons and delete the corresponding component for each of them
        val components = selectedComponents
        // TODO
        components.forEach {
            println(it)
        }
    }

    /**
     * Show a Dialog to ask for delete confirmation.
     *
     * @return true if accepted, false otherwise.
     */
    private fun requestOverrideComponentsDialog(): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        val stage = alert.dialogPane.scene.window as Stage  // TODO
        //stage.icons.add(Image(ShortcutDialogStage::class.java!!.getResourceAsStream("/assets/icon.png")))
        alert.title = resourceBundle.getString("overwrite_button")
        alert.headerText = resourceBundle.getString("overwrite_button_msg")
        alert.contentText = resourceBundle.getString("overwrite_button_msg2")

        val result = alert.showAndWait()
        return if (result.get() === ButtonType.OK) {  // Overwrite
            true
        } else {  // Don't overwite
            false
        }
    }

    fun setHeight(height: Int) {
        this.prefHeight = height.toDouble()
        this.maxHeight = height.toDouble()
        this.minHeight = height.toDouble()
    }

    fun setWidth(width: Int) {
        this.prefWidth = width.toDouble()
        this.maxWidth = width.toDouble()
        this.minWidth = width.toDouble()
    }
}