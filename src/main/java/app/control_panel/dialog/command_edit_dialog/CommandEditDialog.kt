package app.control_panel.dialog.command_edit_dialog

import app.control_panel.ControlPanelStage
import app.control_panel.dialog.command_edit_dialog.builder.BuilderContext
import app.control_panel.dialog.command_edit_dialog.builder.CommandBuilder
import app.control_panel.dialog.command_edit_dialog.builder.annotation.RegisterBuilder
import app.control_panel.dialog.command_edit_dialog.command_type_box.CommandTypeBox
import app.ui.control.*
import app.ui.dialog.OverlayDialog
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.command.Command
import org.reflections.Reflections
import system.applications.Application
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver
import kotlin.reflect.KClass

class CommandEditDialog(controlPanelStage: ControlPanelStage, imageResolver: ImageResolver,
                        val applicationManager: ApplicationManager, val commandManager: CommandManager)
    : OverlayDialog(controlPanelStage, imageResolver), BuilderContext {

    private val builderMap = mutableMapOf<KClass<out Command>, Class<out CommandBuilder>>()

    private var currentBuilder : CommandBuilder? = null

    private val saveButton = SaveButton(imageResolver,"Save")  // TODO: i18n
    private val contentBox = VBox()

    private val imageSelector = ImageSelector(imageResolver)
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
                val builderClass = builderMap[selectedDescriptor.associatedCommandClass.kotlin]
                if (builderClass != null) {
                    currentBuilder = builderClass?.getConstructor(BuilderContext::class.java)?.newInstance(this)

                    // TODO: call command related callbacks

                    builderContainer.children.clear()
                    builderContainer.children.add(currentBuilder?.contentBox)

                    adaptHeight()
                }
            }
        }

        // Load the registered command builder classes
        loadBuilders()

        // Load the UI
        initializeUI()
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