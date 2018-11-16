package app.control_panel.layout_editor_tab.grid

import app.control_panel.dialog.command_edit_dialog.CommandEditDialog
import app.control_panel.layout_editor_tab.grid.button.ComponentButton
import app.control_panel.layout_editor_tab.grid.button.DragButton
import app.control_panel.layout_editor_tab.grid.button.EmptyButton
import app.control_panel.layout_editor_tab.grid.button.SelectableButton
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
import model.parser.component.ComponentParser
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import java.util.*


class ComponentGrid(val parent: BlurrableStage, val componentMatrix: Array<Array<Component?>>,
                    val screenOrientation: ScreenOrientation,
                    val commandManager: CommandManager, val applicationManager: ApplicationManager,
                    override val dndCommandProcessor: DNDCommandProcessor,
                    override val resourceBundle: ResourceBundle,
                    override val imageResolver: ImageResolver, override val componentParser: ComponentParser,
                    override val commandResolver: CommandResolver) : GridPane(), GridContext {

    var onNewComponentRequest: ((Component) -> Unit)? = null
    var onDeleteComponentRequest : ((Component) -> Unit)? = null

    /**
     * @return the row count based on the current screen orientation.
     */
    protected val orientedRowCount: Int
        get() = if (screenOrientation === ScreenOrientation.PORTRAIT) {
            componentMatrix.size
        } else {
            componentMatrix[0].size
        }

    /**
     * @return the col count based on the current screen orientation.
     */
    protected val orientedColCount: Int
        get() = if (screenOrientation === ScreenOrientation.PORTRAIT) {
            componentMatrix[0].size
        } else {
            componentMatrix.size
        }

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
        for (rowIndex in 0 until orientedRowCount) {
            val rc = RowConstraints()
            rc.vgrow = Priority.ALWAYS // allow row to grow
            rc.isFillHeight = true // ask nodes to fill height for row
            rc.percentHeight = 100.0
            // other settings as needed...
            rowConstraints.add(rc)
        }
        for (colIndex in 0 until orientedColCount) {
            val cc = ColumnConstraints()
            cc.hgrow = Priority.ALWAYS // allow column to grow
            cc.isFillWidth = true // ask nodes to fill space for column
            cc.percentWidth = 100.0
            // other settings as needed...
            columnConstraints.add(cc)
        }
    }

    /**
     * Given the col and row in the component matrix, return the actual col in the grid based on
     * the screen orientation.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     * @return the actual col in the grid based on the screen orientation.
     */
    protected fun getOrientedCol(col: Int, row: Int): Int {
        return if (screenOrientation === ScreenOrientation.PORTRAIT) {
            col
        } else {
            row
        }
    }

    /**
     * Given the col and row in the component matrix, return the actual row in the grid based on
     * the screen orientation.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     * @return the actual row in the grid based on the screen orientation.
     */
    protected fun getOrientedRow(col: Int, row: Int): Int {
        return if (screenOrientation === ScreenOrientation.PORTRAIT) {
            row
        } else {
            componentMatrix.size - 1 - col  // Number of columns - 1 - col
        }
    }

    /**
     * Render the componentMatrix into buttons in the GridPane
     */
    fun render() {
        // Delete all the previous nodes
        children.clear()

        // Add all the components
        for (col in componentMatrix.indices) {
            for (row in 0 until componentMatrix[0].size) {
                val button = if (componentMatrix[col][row] != null) {
                    addComponentToGridPane(componentMatrix[col][row])
                } else {
                    addEmptyButtonToGridPane(col, row)
                }

                // Setup common listeners
                button?.onDeselectAllRequested = {
                    if (!it.isShiftDown) {
                        unselectAllButtons()
                    }
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

        current.gridX = col
        current.gridY = row

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
                deleteComponent(component!!, true)
                render()
            }

            // When the component is dropped away, request the
            // deletion from the grid
            override fun onComponentDroppedAway() {
                deleteComponent(component!!, true)
                render()
            }

        }

        current.onDoubleClicked = {
            current.onComponentActionListener?.onComponentEdit()
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
    private fun addButtonToGridPane(col: Int, row: Int, button: DragButton) {
        // Set up the drag and drop
        button.onComponentDropped = {newComponent ->
            var toBeSwapped : Optional<Component> = Optional.empty()

            // The component already present in the newComponent requested position. If null, the position is empty.
            val alreadyPresentComponent = componentMatrix[col][row]

            if (alreadyPresentComponent != null && !newComponent.commandId!!.equals(alreadyPresentComponent!!.commandId!!)) {
                toBeSwapped = Optional.of(alreadyPresentComponent)
            }

            // If a component is present where the drop is going, swap them
            if (toBeSwapped.isPresent()) {
                val oldComponent = toBeSwapped.get()

                // Delete the component
                deleteComponent(oldComponent, true)

                // Replace the oldComponent position with the new one
                oldComponent.x = newComponent.x
                oldComponent.y = newComponent.y

                componentMatrix[oldComponent.x!!][oldComponent.y!!] = oldComponent

                // Notify the listener
                onNewComponentRequest?.invoke(oldComponent)
            }

            // Change the component coordinates
            newComponent.y = row
            newComponent.x = col

            componentMatrix[col][row] = newComponent

            // Notify the listener
            onNewComponentRequest?.invoke(newComponent)

            render()

            true
        }

        // Adjust the grid position based on the orientation
        val gridCol = getOrientedCol(col, row)
        val gridRow = getOrientedRow(col, row)

        // Add the component to the grid
        this.add(button, gridCol, gridRow, 1, 1)
        GridPane.setHalignment(button, HPos.CENTER)
    }

    /**
     * Delete the given component from the componentMatrix, and send a notification to the
     * associated listener.
     *
     * @param component      the component to delete.
     * @param notifyListener if true, the listener will be notified of the deletion.
     */
    private fun deleteComponent(component: Component, notifyListener: Boolean) {
        // Delete the component from the matrix, making sure that the component is the one requested
        if (componentMatrix[component.x!!][component.y!!] == component) {
            componentMatrix[component.x!!][component.y!!] = null
        }

        // Notify the listener
        if (notifyListener) {
            onDeleteComponentRequest?.invoke(component)
        }
    }

    private fun unselectAllButtons() {
        this.children.filter { it is SelectableButton }.forEach {
            it as SelectableButton
            it.selected = false
        }
    }

    fun deleteSelected() {
        // Find all the selected buttons and delete the corresponding component for each of them
        this.children.filter { it is ComponentButton && it.selected }.forEach {
            it as ComponentButton
            deleteComponent(it.associatedComponent, true)
        }

        render()
    }

    fun copySelected() {
        // Find all the selected buttons and delete the corresponding component for each of them
        this.children.filter { it is ComponentButton && it.selected }.forEach {
            it as ComponentButton
            println(it.associatedComponent)
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