package app.control_panel.layout_editor_tab

import app.alert.AlertFactory
import app.control_panel.ControlPanelStage
import app.control_panel.ControlPanelTab
import app.control_panel.DropDialog
import app.control_panel.layout_editor_tab.grid.SectionGrid
import app.control_panel.layout_editor_tab.bar.SectionBar
import app.control_panel.dialog.app_select_dialog.ApplicationSelectDialog
import app.control_panel.layout_editor_tab.action.ActionManager
import app.control_panel.layout_editor_tab.action.ActionReceiver
import app.control_panel.layout_editor_tab.action.model.Action
import app.control_panel.layout_editor_tab.action.model.SectionRelatedAction
import io.reactivex.subjects.PublishSubject
import javafx.animation.*
import javafx.scene.CacheHint
import javafx.scene.control.ScrollPane
import javafx.scene.input.*
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.util.Duration
import model.page.DefaultPage
import model.parser.component.ComponentParser
import model.section.Section
import system.BroadcastManager
import system.commands.CommandManager
import system.drag_and_drop.DNDCommandProcessor
import system.image.ImageResolver
import system.applications.ApplicationManager
import system.exceptions.IncompatibleOsException
import system.section.SectionManager
import system.section.exporter.SectionExporter
import system.section.importer.RelatedApplicationNotFoundException
import system.section.importer.SectionImporter
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

const val MAX_PAGES = 6

class LayoutEditorTab(val controlPanelStage: ControlPanelStage, val sectionManager: SectionManager,
                      val imageResolver: ImageResolver, val resourceBundle: ResourceBundle,
                      val componentParser: ComponentParser, val commandManager: CommandManager,
                      val applicationManager: ApplicationManager,
                      val dndCommandProcessor: DNDCommandProcessor, val sectionExporter: SectionExporter,
                      val sectionImporter: SectionImporter) : ControlPanelTab(), ActionReceiver {

    val layoutToolbar = LayoutToolbar(imageResolver, applicationManager)
    val sectionBar: SectionBar
    var sectionGrid: SectionGrid? = null
    val sectionGridContainer: ScrollPane = ScrollPane()  // Used as a workaround to fix overflowing transitions

    // Used to save section edits with a debouncing mechanism
    val saveSectionSubject = PublishSubject.create<Section>()

    // Reference to the dialog that opens when dragging a section file inside
    private var dropDialog : DropDialog? = null

    // Action manager used to implement the Undo/Redo functionality
    private val actionManager = ActionManager()

    // Used to manage Copy/Paste operations of Components
    private val copyManager = CopyManager()

    init {
        children.add(layoutToolbar)

        // The section bar must be included in a box as a workaround to add the gradient background without
        // the focus border of javafx
        val sectionBarContainer = VBox()
        sectionBarContainer.isCache = true
        sectionBarContainer.cacheHint = CacheHint.SPEED
        sectionBarContainer.styleClass.add("app_scroll_pane_container")
        sectionBar = SectionBar(sectionManager, applicationManager, imageResolver, commandManager)
        sectionBarContainer.children.add(sectionBar)

        children.add(sectionBarContainer)

        sectionGridContainer.styleClass.add("grid_scroll_pane_container")
        sectionGridContainer.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        sectionGridContainer.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        sectionGridContainer.isFitToHeight = true
        sectionGridContainer.isFitToWidth = true
        children.add(sectionGridContainer)

        sectionBar.onSectionClicked = { section ->
            loadSection(section)
        }
        sectionBar.onDeleteRequest = { section ->
            requestDeleteSection(section)
        }
        sectionBar.onResetRequest = { section ->
            requestResetSection(section)
        }
        sectionBar.onExportRequest = { section ->
            val fileChooser = FileChooser()
            fileChooser.title = "Export section..."  // TODO: i18n
            fileChooser.initialDirectory = File(System.getProperty("user.home"))
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Layout Format", "*.dklf"))
            fileChooser.initialFileName = "${section.name}.dklf"
            val destinationFile = fileChooser.showSaveDialog(null)
            if (destinationFile != null) {
                sectionExporter.export(section, destinationFile)
            }
        }
        sectionBar.onImportRequest = {
            val fileChooser = FileChooser()
            fileChooser.title = "Import section..."  // TODO: i18n
            fileChooser.initialDirectory = File(System.getProperty("user.home"))
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dokey Layout Format", "*.dklf"))

            val sourceFile = fileChooser.showOpenDialog(null)
            if (sourceFile != null) {
                requestImportSection(sourceFile)
            }
        }

        // Setup the debouncing save mechanism
        saveSectionSubject.debounce(100, TimeUnit.MILLISECONDS).subscribe { section ->
            // Ssve the section
            sectionManager.saveSection(section)

            // Send a broadcast
            BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, section.id)
        }

        // Button click event listeners
        layoutToolbar.onNewLayoutRequested = {
            val dialog = ApplicationSelectDialog(controlPanelStage, imageResolver, applicationManager)
            dialog.onApplicationSelected = { app ->
                // Create the section
                val newSection = sectionManager.createSectionForApp(app)

                // Reload the bar
                sectionBar.loadSections(targetSection = newSection)
            }
            dialog.showWithAnimation()
        }
        layoutToolbar.onMoreBtnClicked = {event ->
            // Get the context menu for the current selector
            sectionBar.currentSelector?.contextMenu?.show(layoutToolbar, event.screenX-50, event.screenY)
        }

        // Setup the drag and drop for layout file importing
        setOnDragEntered {
            if (dropDialog == null) {
                // Check if the dropping file is a dokey exported layout
                if (validateFileDropPayload(it)) {
                    dropDialog = DropDialog(controlPanelStage, imageResolver)
                    dropDialog?.verifyPayload = this::validateFileDropPayload
                    dropDialog?.showWithAnimation()
                    dropDialog?.onDialogClosed = {
                        dropDialog = null
                    }
                    dropDialog?.onContentDropped = {dragboard ->
                        if (dragboard.hasFiles() && dragboard.files[0].isFile) {
                            requestImportSection(dragboard.files[0])
                        }
                    }
                }
            }
        }

        actionManager.onSectionModified = { section ->
            onSectionModified(section)
        }
    }

    override fun notifyAction(action: Action) {
        actionManager.execute(action)

        // If the action is section related, notify the sections change
        if (action is SectionRelatedAction) {
            action.relatedSections.forEach { section ->
                onSectionModified(section)
            }
        }
    }

    override fun onFocus() {
        // Select the first one
        sectionBar.selectSection(0)
    }

    private fun onSectionModified(section: Section) {
        // If the current section grid is related to this action, render it again
        if (sectionGrid?.section == section) {
            sectionGrid?.invalidate()
        }

        // Save the section
        saveSectionSubject.onNext(section)
    }

    // Key Shortcuts
    val undoKeystrokeCombination = KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)
    val redoKeystrokeCombination = KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)

    override fun onGlobalKeyPress(event: KeyEvent) {
        if (undoKeystrokeCombination.match(event)) {
            actionManager.undo()
        }else if (redoKeystrokeCombination.match(event)) {
            actionManager.redo()
        }else{
            // Forword the event to the section grid
            sectionGrid?.onKeyPress(event)
        }
    }

    fun requestSection(targetSectionId: String?) {
        if (targetSectionId != null) {
            sectionBar.selectSection(targetSectionId, canReloadSections = true)
        }
    }

    private fun loadSection(section: Section) {
        val oldGrid = sectionGrid

        // Create the section grid
        sectionGrid = SectionGrid(controlPanelStage, section, imageResolver, resourceBundle, componentParser,
                commandManager, applicationManager, dndCommandProcessor, this, sectionManager,
                copyManager)
        sectionGrid!!.onSectionModified = { section ->
            saveSectionSubject.onNext(section)
        }
        sectionGrid!!.onRequestAddPage = { section ->
            // Make sure to not exceed the limit
            if (section.pages!!.size < MAX_PAGES) {
                val newPage = DefaultPage()
                newPage.components = mutableListOf()
                newPage.colCount = SectionManager.DEFAULT_PAGE_COLS
                newPage.rowCount = SectionManager.DEFAULT_PAGE_ROWS
                section.pages!!.add(newPage)

                sectionManager.saveSection(section)

                sectionGrid!!.invalidate()
            }
        }

        layoutToolbar.setCurrentSection(section)

        // Replace the old grid with the new one, transitioning if necessary
        if (oldGrid != null) {
            slideAnimation(sectionGrid!!)
        } else {
            sectionGridContainer.content = sectionGrid
        }
    }

    private fun slideAnimation(newGrid: SectionGrid) {
        sectionGridContainer.content = newGrid

        newGrid.isCache = true
        newGrid.cacheHint = CacheHint.SPEED

        val SLIDE_DURATION = 0.3

        val slideIn = TranslateTransition(
                Duration.seconds(SLIDE_DURATION), newGrid)
        slideIn.interpolator = Interpolator.EASE_BOTH
        slideIn.fromY = 30.0
        slideIn.toY = 0.0

        val fadeIn = FadeTransition(
                Duration.seconds(SLIDE_DURATION), newGrid)
        fadeIn.interpolator = Interpolator.EASE_BOTH
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        val crossFade = ParallelTransition(
                slideIn, fadeIn)
        crossFade.play()

        crossFade.setOnFinished { newGrid.cacheHint = CacheHint.QUALITY; newGrid.isCache = false }
    }

    private fun requestDeleteSection(section: Section) {// TODO: i18n
        AlertFactory.instance.confirmation("Delete confirmation", "Do you really want to delete ${section.name} layout?",
                onYes = {
                    sectionManager.deleteSection(section)
                    sectionBar.loadSections()
                }).show()
    }

    private fun requestResetSection(section: Section) {// TODO: i18n
        AlertFactory.instance.confirmation("Reset confirmation", "Do you really want to reset ${section.name} layout? All your changes will be overwritten.",
                onYes = {
                    val resettedSection = sectionManager.resetSection(section)
                    sectionBar.loadSections(targetSection = resettedSection)

                    // Send a broadcast
                    BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, resettedSection?.id)
                }).show()
    }

    private fun requestImportSection(sourceFile: File) {
        // Check if the section already exists and would be overwritten with the import
        if (sectionImporter.checkIfSectionAlreadyExists(sourceFile)) {
            AlertFactory.instance.confirmation("Layout already exist",  // TODO: i18n
                    "The layout you're trying to import already exist and will be overwritten. " +
                            "Do you want to proceed anyway?",
                    onYes = {
                        importSection(sourceFile)
                    }).show()
        }else{
            importSection(sourceFile)
        }
    }

    private fun importSection(sourceFile: File) {
        try {
            val sectionResult = sectionImporter.import(sourceFile)
            if (sectionResult.section != null) {
                // Show an alert if some commands could not be imported
                if (sectionResult.failedCommands.isNotEmpty()) {
                    AlertFactory.instance.alert("Warning",  // TODO: i18n
                            "Some commands cannot not be imported because they are incompatible with your system: \n\n" +
                                    "${sectionResult.failedCommands.map { "- " + it.title }.joinToString(separator = "\n")}"
                    ).show()
                }

                sectionBar.loadSections(targetSection = sectionResult.section)
            }
        }catch(ex: IncompatibleOsException) {
            AlertFactory.instance.alert("Incompatible layout",  // TODO: i18n
                    "Cannot import the requested layout because it is not compatible with your system."
            ).show()
        }catch(ex: RelatedApplicationNotFoundException) {
            AlertFactory.instance.alert("Application not found",  // TODO: i18n
                    "Cannot import the requested layout because the associated application is not installed in your system."
            ).show()
        }
    }

    private fun validateFileDropPayload(event: DragEvent) : Boolean {
        return event.dragboard.hasFiles() && event.dragboard.files[0].isFile && event.dragboard.files[0].extension == "dklf"
    }
}