package app.ui.dialog

import app.control_panel.ControlPanelStage
import app.ui.animation.StageOpacityTransition
import app.ui.animation.StagePositionTransition
import app.ui.control.IconButton
import javafx.animation.ParallelTransition
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import system.ResourceUtils
import system.image.ImageResolver

open class OverlayDialog(val controlPanelStage: ControlPanelStage, val imageResolver: ImageResolver) : Stage() {
    private val parentBox = VBox()
    private val topSection = HBox()
    private val closeBtn = IconButton(imageResolver, "asset:x-circle", 20)

    init {
        scene = Scene(parentBox)
        scene.stylesheets.add(ResourceUtils.getResource("/css/dialog.css")!!.toURI().toString())
        this.title = "Dokey"
        this.scene = scene
        this.isAlwaysOnTop = true
        initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        this.icons.add(Image(OverlayDialog::class.java.getResourceAsStream("/assets/icon.png")))

        width = 380.0
        height = 570.0

        // Position the dialog over the stage
        x = controlPanelStage.x + (controlPanelStage.width - width) / 2
        y = controlPanelStage.y + (controlPanelStage.height - height) / 2

        // Blur the control panel stage
        Platform.runLater { controlPanelStage.blurIn() }

        // Close the stage when unfocused
        focusedProperty().addListener { _, _, isFocused ->
            if (!isFocused) {
                onClose()
            }
        }
    }

    fun initializeUI() {
        // Load the top section
        topSection.styleClass.add("top-section")
        val spacerPane = Pane()
        HBox.setHgrow(spacerPane, Priority.ALWAYS)
        val topLeftComponent = defineTopSectionComponent()
        topLeftComponent?.let { topSection.children.add(it) }
        topSection.children.addAll(spacerPane, closeBtn)
        parentBox.children.add(topSection)

        val contentBox = defineContentBoxComponent()
        contentBox?.let { parentBox.children.add(it) }

        // Close button event
        closeBtn.setOnAction {
            controlPanelStage.requestFocus()
            onClose()
        }
    }

    open protected fun defineTopSectionComponent() : Node? = null
    open protected fun defineContentBoxComponent() : VBox? = null

    open fun onClose() {
        closeWithAnimation()
        Platform.runLater { controlPanelStage.blurOut() }
    }

    fun showWithAnimation() {
        show()

        val fadeTransition = StageOpacityTransition(Duration.millis(200.0), this)
        val positionTransition = StagePositionTransition(Duration.millis(200.0), this)
        positionTransition.toY = this.y
        positionTransition.fromY = this.y + 25

        val transition = ParallelTransition(fadeTransition, positionTransition)
        transition.play()
    }

    fun closeWithAnimation() {
        val fadeTransition = StageOpacityTransition(Duration.millis(200.0), this)
        fadeTransition.to = 0.0
        val positionTransition = StagePositionTransition(Duration.millis(200.0), this)
        positionTransition.toY = this.y + 25.0
        positionTransition.fromY = this.y

        val transition = ParallelTransition(fadeTransition, positionTransition)
        transition.setOnFinished { close() }
        transition.play()
    }
}