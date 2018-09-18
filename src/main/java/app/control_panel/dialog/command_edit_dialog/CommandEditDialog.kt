package app.control_panel.dialog.command_edit_dialog

import app.control_panel.ControlPanelStage
import app.control_panel.dialog.app_select_dialog.ApplicationSelectDialog
import app.control_panel.dialog.command_edit_dialog.builder.BuilderContext
import app.control_panel.dialog.command_edit_dialog.builder.CommandBuilder
import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.command_type_box.CommandTypeBox
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.*
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.command.Command
import org.reflections.Reflections
import system.BroadcastManager
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver
import kotlin.reflect.KClass

class CommandEditDialog(parent: BlurrableStage, imageResolver: ImageResolver,
                        override val applicationManager: ApplicationManager, val commandManager: CommandManager)
    : OverlayDialog(parent, imageResolver), BuilderContext {

    private val builderMap = mutableMapOf<KClass<out Command>, Class<out CommandBuilder>>()

    private var currentBuilder: CommandBuilder? = null

    private var currentCommand: Command? = null

    private val saveButton = SaveButton(imageResolver, "Save")  // TODO: i18n
    private val contentBox = VBox()

    private val imageSelector = ImageSelector(this, imageResolver)
    private val titleTextField = StyledTextField()
    private val descriptionTextField = StyledTextArea()
    private val advancedPane = VBox()

    private val quickCommandPane = HBox()
    private val quickCommandTextField = ColonTextField()
    private val quickCommandIcon = IconButton(imageResolver, "asset:zap", 16)

    private val expandButton = CollapseExpandButton(imageResolver, "Advanced", "Less")  // TODO: i18n
    private val commandTypeBox = CommandTypeBox(imageResolver)

    private val builderContainer = VBox()

    init {
        contentBox.alignment = Pos.TOP_CENTER
        contentBox.styleClass.add("command-edit-dialog-contentbox")

        titleTextField.promptText = "Insert name..."  // TODO: i18n
        titleTextField.alignment = Pos.CENTER
        titleTextField.styleClass.add("command-edit-dialog-title-field")

        descriptionTextField.promptText = "Insert description..."  // TODO: i18n
        descriptionTextField.isWrapText = true
        descriptionTextField.styleClass.add("command-edit-dialog-desc-field")

        quickCommandTextField.maxWidth = 100.0
        quickCommandPane.alignment = Pos.CENTER
        quickCommandPane.children.addAll(quickCommandTextField, quickCommandIcon)

        advancedPane.children.add(quickCommandPane)

        advancedPane.isManaged = false
        advancedPane.isVisible = false

        contentBox.children.addAll(imageSelector, titleTextField, descriptionTextField, advancedPane,
                expandButton, commandTypeBox, builderContainer)

        expandButton.onExpand = {
            advancedPane.isManaged = true
            advancedPane.isVisible = true

            adaptHeight()
        }
        expandButton.onCollapse = {
            advancedPane.isManaged = false
            advancedPane.isVisible = false

            adaptHeight()
        }

        commandTypeBox.setOnAction {
            val selectedDescriptor = commandTypeBox.selectionModel.selectedItem
            if (selectedDescriptor != null) {
                loadBuilderForCommandClass(selectedDescriptor.associatedCommandClass.kotlin)

                adaptHeight()
            }
        }

        saveButton.setOnAction {
            val commandId = requestSave()
            if (commandId >= 0) { // Command saved correctly
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_COMMAND_EVENT, commandId.toString())

                onClose()
            }
        }

        // Load the registered command builder classes
        loadBuilders()

        // Load the UI
        initializeUI()
    }

    fun loadCommand(command: Command) {
        currentCommand = command

        titleTextField.text = command.title

        command.description?.let {
            descriptionTextField.text = it
        }

        command.quickCommand?.let {
            quickCommandTextField.text = it
        }

        command.iconId?.let {
            imageSelector.imageId = it
        }

        // Select the correct combobox entry
        commandTypeBox.selectTypeForCommand(command)

        // Disable the selection of type for the command modification
        commandTypeBox.isDisable = true

        // Load the correct builder UI
        loadBuilderForCommandClass(command::class)

        // Inject the command to the builder UI
        currentBuilder?.populateUIForCommand(command)
    }

    /**
     * @return the command id if saved or -1 if an error occurred
     */
    private fun requestSave(): Int {
        try {
            // Validate the command and then save
            validate()
            return saveCommand()
        } catch (e: ValidationException) {
            // TODO: message box here
            println(e.errorMessage)
        }

        return -1
    }

    private fun saveCommand() : Int {
        val command = if (currentCommand != null) {
            currentCommand!!
        } else {
            // New command, create an instance of the correct class
            val selectedDescriptor = commandTypeBox.selectionModel.selectedItem
            selectedDescriptor.associatedCommandClass.newInstance()!!
        }

        command.title = titleTextField.text

        if (!descriptionTextField.text.isBlank()) {
            command.description = descriptionTextField.text
        }else{
            command.description = null
        }

        if (!quickCommandTextField.text.isBlank()) {
            command.quickCommand = quickCommandTextField.text
        }else{
            command.quickCommand = null
        }

        if (imageSelector.imageId != null) {
            command.iconId = imageSelector.imageId
        }else{
            command.iconId = null
        }

        // Update the command type specific fields using the builder
        currentBuilder?.updateCommand(command)

        if (currentCommand == null) {  // Creating a new command
            val result = commandManager.addCommand(command)
            return result.id!!
        } else {  // Editing an existing command
            commandManager.saveCommand(command)
            return command.id!!
        }
    }

    private fun validate() {
        if (titleTextField.text.isBlank()) {
            throw ValidationException("Please insert the name.")  // TODO: i18n
        }

        // Make sure that a command type is selected
        if (commandTypeBox.selectionModel.selectedItem == null) {
            throw ValidationException("Please select a command type.")  // TODO: i18n
        }

        // Validate command specific types
        currentBuilder?.validateInput()
    }

    private fun loadBuilderForCommandClass(commandClass: KClass<out Command>) {
        val builderClass = builderMap[commandClass]
        if (builderClass != null) {
            currentBuilder = builderClass?.getConstructor(BuilderContext::class.java, BlurrableStage::class.java)
                    ?.newInstance(this, this)

            builderContainer.children.clear()
            builderContainer.children.add(currentBuilder?.contentBox)
        }
    }

    private fun loadBuilders() {
        // Load all the command handlers dynamically
        val reflections = Reflections("app.control_panel.dialog.command_edit_dialog.builder")
        val commands = reflections.getTypesAnnotatedWith(RegisterBuilder::class.java)
        commands.forEach { commandClass ->
            val annotation = commandClass.getAnnotation(RegisterBuilder::class.java)
            builderMap[annotation.type] = commandClass as Class<out CommandBuilder>
        }
    }

    override fun defineTopSectionComponent(): Node? {
        return saveButton
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}