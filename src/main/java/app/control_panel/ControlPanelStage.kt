package app.control_panel

import app.ui.animation.BlurTransition
import app.ui.animation.StageOpacityTransition
import app.ui.animation.StagePositionTransition
import app.control_panel.command_tab.CommandTab
import app.control_panel.controllers.ControlPanelController
import app.control_panel.devices_tab.DevicesTab
import app.control_panel.layout_editor_tab.GlobalKeyboardListener
import app.control_panel.layout_editor_tab.LayoutEditorTab
import app.control_panel.settings_tab.SettingsTab
import app.control_panel.tab_selector.TabSelector
import app.ui.stage.BlurrableStage
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXMLLoader
import javafx.scene.CacheHint
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Tab
import javafx.scene.effect.Effect
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import javafx.util.Duration
import model.parser.component.ComponentParser
import system.BroadcastManager
import system.ResourceUtils
import system.SettingsManager
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import system.applications.ApplicationManager
import system.commands.exporter.CommandExporter
import system.commands.importer.CommandImporter
import system.section.SectionManager
import system.section.exporter.SectionExporter
import system.section.importer.SectionImporter
import system.server.HandshakeDataBuilder
import system.startup.StartupManager
import system.storage.StorageManager
import utils.OSValidator
import java.util.*

class ControlPanelStage(val sectionManager: SectionManager, val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                        val componentParser: ComponentParser, val commandManager: CommandManager,
                        val applicationManager: ApplicationManager, val handshakeDataBuilder: HandshakeDataBuilder,
                        val dndCommandProcessor: DNDCommandProcessor, val settingsManager: SettingsManager,
                        val startupManager: StartupManager, val storageManager: StorageManager,
                        val commandExporter: CommandExporter, val commandImporter: CommandImporter,
                        val sectionExporter: SectionExporter, val sectionImporter: SectionImporter)
    : BlurrableStage(), GlobalKeyboardListener {

    private val controller : ControlPanelController

    private val layoutEditorTab = LayoutEditorTab(this, sectionManager, imageResolver, resourceBundle, componentParser,
            commandManager, applicationManager, this, dndCommandProcessor, sectionExporter,
            sectionImporter)

    private val devicesTab = DevicesTab(imageResolver, resourceBundle, handshakeDataBuilder)

    private val commandTab = CommandTab(this, imageResolver, resourceBundle, applicationManager,
            commandManager, commandExporter, commandImporter, settingsManager, dndCommandProcessor)

    private val settingsTab = SettingsTab(imageResolver, applicationManager, settingsManager, startupManager,
            resourceBundle, storageManager)

    private val tabs = listOf<ControlPanelTab>(devicesTab, layoutEditorTab, commandTab,
            ComingSoonTab(), settingsTab)

    // This variable will hold the currently active control panel tab
    private var activeTab : ControlPanelTab

    override var isShiftPressed: Boolean = false

    private val tabSelector = TabSelector(imageResolver)

    override val parent: BlurrableStage? = null

    init {
        val fxmlLoader = FXMLLoader(ResourceUtils.getResource("/layouts/control_panel.fxml")!!.toURI().toURL())
        fxmlLoader.resources = resourceBundle
        val root = fxmlLoader.load<Parent>()
        val scene = Scene(root)
        scene.stylesheets.add(ResourceUtils.getResource("/css/control_panel.css")!!.toURI().toString())
        this.title = "Dokey Control Panel"
        this.scene = scene
        this.isAlwaysOnTop = true
        this.isResizable = false
        initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        this.icons.add(Image(ControlPanelStage::class.java.getResourceAsStream("/assets/icon.png")))

        controller = fxmlLoader.getController<Any>() as ControlPanelController

        // Initialize the style
        initializeStyle()

        // Load the tab selector
        controller.top_section.children.add(tabSelector)

        // Load the tabs
        tabs.forEach {
            val tab = Tab()
            tab.content = it
            controller.tab_pane.tabs.add(tab)
        }

        // Initially select the first one
        activeTab = tabs[0]
        activeTab.onFocus()

        // Setup the tab change listener
        tabSelector.onTabSelected = {tabIndex ->
            if (controller.tab_pane.selectionModel.selectedIndex != tabIndex) {
                activeTab.onUnfocus()  // Send the unfocus event to the previous one
                activeTab = tabs[tabIndex]
                activeTab.onFocus()  // Send the focus event to the current

                controller.tab_pane.selectionModel.select(tabIndex)
            }
        }

        // Setup the tab change animation
        setupTabPaneAnimation()

        // Keyboard listeners for detecting if a key is pressed or not
        scene.setOnKeyPressed {
            if (it.code == KeyCode.SHIFT) {
                isShiftPressed = true
            }
        }
        scene.setOnKeyReleased {
            if (it.code == KeyCode.SHIFT) {
                isShiftPressed = false
            }else if (it.code == KeyCode.ESCAPE) {  // Close the dialog on ESC key pressed
                animateOut(null)
            }

            // Notify the active tab
            activeTab.onGlobalKeyPress(it)
        }
    }

    fun selectTab(tabIndex: Int) {
        tabSelector.selectTab(tabIndex)
    }

    fun requestSectionFocus(targetSection: String?) {
        val editorTabIndex = tabs.indexOfFirst { it == layoutEditorTab }
        if (editorTabIndex >= 0) {
            selectTab(editorTabIndex)
            layoutEditorTab.requestSection(targetSection)
        }
    }

    private fun setupTabPaneAnimation() {
        // Transition animation
        controller.tab_pane.selectionModel
                .selectedItemProperty()
                .addListener { _, oldTab, newTab ->
                    newTab.content.isCache = true
                    newTab.content.cacheHint = CacheHint.SPEED

                    val fadeInTransition = FadeTransition(Duration(300.0), newTab.content)
                    fadeInTransition.fromValue = 0.0
                    fadeInTransition.toValue = 1.0

                    val translateTransition = TranslateTransition(Duration(300.0), newTab.content)
                    translateTransition.interpolator = Interpolator.EASE_BOTH
                    translateTransition.toY = 0.0
                    translateTransition.fromY = 20.0

                    val parallelTransition = ParallelTransition(fadeInTransition, translateTransition)
                    parallelTransition.setOnFinished {
                        newTab.content.isCache = false
                        activeTab.onAnimatedIn()
                    }
                    parallelTransition.play()
                }
    }

    fun animateIn() {
        // Reset the effects of the control panel
        controller.parent_box.effect = null

        val fadeTransition = StageOpacityTransition(Duration.millis(200.0), this)
        val positionTransition = StagePositionTransition(Duration.millis(200.0), this)
        positionTransition.toY = this.y
        positionTransition.fromY = this.y + 25

        val transition = ParallelTransition(fadeTransition, positionTransition)
        transition.play()

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.CONTROL_PANEL_OPENED_EVENT, null)
    }

    fun animateOut(onFinished: (() -> Unit)?) {
        val fadeTransition = StageOpacityTransition(Duration.millis(200.0), this)
        fadeTransition.to = 0.0
        val positionTransition = StagePositionTransition(Duration.millis(200.0), this)
        positionTransition.toY = this.y + 25.0
        positionTransition.fromY = this.y

        val transition = ParallelTransition(fadeTransition, positionTransition)
        transition.setOnFinished {
            onFinished?.invoke()
            close()
        }
        transition.play()

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.CONTROL_PANEL_CLOSED_EVENT, null)
    }

    override fun blurIn() {
        val transition = BlurTransition(controller.parent_box, Duration(200.0), 0.0, 10.0,
                0.0, -0.2)
        transition.play()
    }

    override fun blurOut() {
        val transition = BlurTransition(controller.parent_box, Duration(200.0), 10.0, 0.0,
                -0.2, 0.0)
        transition.play()
    }

    private fun initializeStyle() {
        if (OSValidator.isWindows()) {
            this.scene.root.styleClass.add("windows-panel")
        }else if (OSValidator.isMac()) {
            this.scene.root.styleClass.add("mac-panel")
        }
    }
}