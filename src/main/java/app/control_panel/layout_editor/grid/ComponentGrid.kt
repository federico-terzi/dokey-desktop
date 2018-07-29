package app.control_panel.layout_editor.grid

import app.control_panel.layout_editor.grid.button.ComponentButton
import app.control_panel.layout_editor.grid.button.DragButton
import app.control_panel.layout_editor.grid.button.EmptyButton
import app.control_panel.layout_editor.model.ScreenOrientation
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
import system.image.ImageResolver
import java.lang.reflect.InvocationTargetException
import java.util.*


class ComponentGrid(val componentMatrix: Array<Array<Component?>>,
                    val screenOrientation: ScreenOrientation, override val resourceBundle: ResourceBundle,
                    override val imageResolver: ImageResolver, override val componentParser: ComponentParser,
                    override val commandResolver: CommandResolver) : GridPane(), GridContext {
    var onComponentSelectedListener: OnComponentSelectedListener? = null

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
    private fun addEmptyButtonToGridPane(col: Int, row: Int) {
        val emptyButton = EmptyButton(this)
        addButtonToGridPane(col, row, emptyButton)
    }

    /**
     * Add a component to the GridPane.
     *
     * @param component the component to add.
     * @return
     */
    fun addComponentToGridPane(component: Component?) {
        // Make sure the component is not null
        if (component == null)
            return

        // Extract the coordinates from the component
        val col = component!!.x!!
        val row = component!!.y!!

        // Get the current button
        val current = ComponentButton(this, component)

        current.onComponentActionListener = object : ComponentButton.OnComponentActionListener {
            override fun onComponentEdit() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        addButtonToGridPane(col, row, current)
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
        button.onComponentDragListener = object : DragButton.OnComponentDragListener {
            override fun onComponentDropped(newComponent: Component): Boolean {
                var toBeDeleted : Optional<Component> = Optional.empty()

                // The component already present in the newComponent requested position. If null, the position is empty.
                val alreadyPresentComponent = componentMatrix[col][row]

                if (alreadyPresentComponent != null && !newComponent.commandId!!.equals(alreadyPresentComponent!!.commandId!!)) {
                    toBeDeleted = Optional.of(alreadyPresentComponent)
                }

                // If the component will overwrite a button, ask for confirmation
                if (toBeDeleted.isPresent()) {
                    if (!requestOverrideComponentsDialog()) {  // DONT OVERWRITE
                        return false
                    }

                    // Delete the component
                    deleteComponent(toBeDeleted.get(), false)
                }

                // Change the component coordinates
                newComponent.y = row
                newComponent.x = col

                componentMatrix[col][row] = newComponent

                // Notify the listener
                if (onComponentSelectedListener != null) {
                    onComponentSelectedListener!!.onNewComponentRequested(newComponent)
                }

                render()

                return true
            }

            override fun onComponentDropping(component: Component): Boolean {
                // Determine if the dropping can cause an overwrite.
                var isDangerous = true
                if (button is EmptyButton) {
                    isDangerous = false
                }

                // Select the component
                button.setDragDestination(true, isDangerous)
                return false
            }
        }

        // Adjust the grid position based on the orientation
        val gridCol = getOrientedCol(col, row)
        val gridRow = getOrientedRow(col, row)

        // Add the component to the grid
        this.add(button, gridCol, gridRow, 1, 1)
    }

    /**
     * Delete the given component from the componentMatrix, and send a notification to the
     * associated listener.
     *
     * @param component      the component to delete.
     * @param notifyListener if true, the listener will be notified of the deletion.
     */
    private fun deleteComponent(component: Component, notifyListener: Boolean) {
        // Delete the component from the matrix
        componentMatrix[component.x!!][component.y!!] = null

        // Notify the listener
        if (onComponentSelectedListener != null && notifyListener) {
            onComponentSelectedListener!!.onDeleteComponentRequested(component)
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

    interface OnComponentSelectedListener {
        fun onNewComponentRequested(component: Component)

        fun onDeleteComponentRequested(component: Component)

        fun onEditComponentRequested(component: Component)
    }
}