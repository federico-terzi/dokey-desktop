package app.control_panel.dialog.command_edit_dialog

import app.alert.AlertFactory
import app.control_panel.dialog.command_edit_dialog.builder.BuilderContext
import app.control_panel.dialog.command_edit_dialog.builder.CommandBuilder
import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.validation.ValidationException
import app.ui.control.*
import app.ui.dialog.OverlayDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.command.Command
import org.reflections.Reflections
import system.BroadcastManager
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.commands.model.CommandWrapper
import system.image.ImageResolver
import kotlin.reflect.KClass

class CommandEditDialog(parent: BlurrableStage, imageResolver: ImageResolver,
                        override val applicationManager: ApplicationManager, val commandManager: CommandManager)
    : OverlayDialog(parent, imageResolver), BuilderContext {

    private val builderMap = mutableMapOf<KClass<out Command>, Class<out CommandBuilder>>()

    private var currentBuilder: CommandBuilder? = null

    private var currentCommand: Command? = null

    private val saveButton = SaveButton(imageResolver, "Save")  // TODO: i18n
    private val deleteButton = DeleteButton(imageResolver, "Delete")  // TODO: i18n
    private val lockImage = IconButton(imageResolver, "asset:lock", 18, noPadding = true)
    private val contentBox = VBox()

    private val imageSelector = ImageSelector(this, imageResolver)
    private val titleTextField = StyledTextField()
    private val descriptionTextField = StyledTextArea()
    private val advancedPane = VBox()

    private val quickCommandPane = HBox()
    private val quickCommandTextField = ColonTextField()
    private val quickCommandIcon = IconButton(imageResolver, "asset:zap", 16)

    private val expandButton = CollapseExpandButton(imageResolver, "Advanced", "Less")  // TODO: i18n
    private val commandTypeButton = CommandTypeButton(this, imageResolver)

    private val builderContainer = VBox()

    var onCommandSaved : ((Command) -> Unit)? = null
    var onCommandDeleteRequest : ((Command) -> Unit)? = null

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

        lockImage.isVisible = false
        deleteButton.isVisible = false
        deleteButton.isManaged = false

        contentBox.children.addAll(imageSelector, titleTextField, descriptionTextField, advancedPane,
                expandButton, commandTypeButton, builderContainer)

        expandButton.onExpand = {
            expandAdvanced()

        }
        expandButton.onCollapse = {
            collapseAdvanced()
        }

        commandTypeButton.onTypeSelected = {descriptor ->
            loadBuilderForCommandClass(descriptor.associatedCommandClass.kotlin)

            adaptHeight()
        }

        saveButton.setOnAction {
            val commandId = requestSave()
            if (commandId >= 0) { // Command saved correctly
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_COMMAND_EVENT, commandId.toString())

                onCommandSaved?.invoke(commandManager.getCommand(commandId)!!)

                onClose()
            }
        }

        deleteButton.setOnAction {
            if (currentCommand != null) {
                onCommandDeleteRequest?.invoke(currentCommand!!)
            }
        }

        // Load the registered command builder classes
        loadBuilders()

        // Load the UI
        initializeUI()
    }

    fun loadCommand(command: Command) {
        command as CommandWrapper  // Used to obtain the wrapper information

        currentCommand = command

        titleTextField.text = command.title

        command.description?.let {
            descriptionTextField.text = it
        }

        command.quickCommand?.let {
            quickCommandTextField.text = it

            // If quick command is present also expand the advanced pane
            expandAdvanced()
        }

        command.iconId?.let {
            imageSelector.imageId = it
        }

        // Select the correct combobox entry
        commandTypeButton.selectTypeForCommand(command)

        // Disable the selection of type for the command modification
        commandTypeButton.isDisable = true

        // If the command is locked, disable all inputs except the quick command
        if (command.locked) {
            titleTextField.isDisable = true
            descriptionTextField.isDisable = true
            imageSelector.isDisable = true

            // If locked also show the advanced pane to change the quick command
            expandAdvanced()

            // If locked, show the lock one
            lockImage.isVisible = true
        }else{
            deleteButton.isVisible = true
            deleteButton.isManaged = true
        }

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
            AlertFactory.instance.alert("Invalid command", e.errorMessage).show()  // TODO: i18n
        }

        return -1
    }

    private fun saveCommand() : Int {
        val command = if (currentCommand != null) {
            currentCommand!!
        } else {
            // New command, create an instance of the correct class
            val selectedDescriptor = commandTypeButton.commandDescriptor!!
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
        if (commandTypeButton.commandDescriptor == null) {
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

    private fun expandAdvanced() {
        advancedPane.isManaged = true
        advancedPane.isVisible = true

        expandButton.collapsed = false
        expandButton.render()

        adaptHeight()
    }

    private fun collapseAdvanced() {
        advancedPane.isManaged = false
        advancedPane.isVisible = false

        expandButton.collapsed = true
        expandButton.render()

        adaptHeight()
    }

    override fun defineTopSectionComponent(): Node? {
        val hbox = HBox()
        hbox.spacing = 6.0
        hbox.alignment = Pos.CENTER_LEFT
        hbox.children.addAll(saveButton, deleteButton, lockImage)
        return hbox
    }

    override fun defineContentBoxComponent(): VBox? {
        return contentBox
    }
}