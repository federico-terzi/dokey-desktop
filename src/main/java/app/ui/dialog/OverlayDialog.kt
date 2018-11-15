package app.ui.dialog

import app.ui.animation.BlurTransition
import app.ui.animation.StageOpacityTransition
import app.ui.animation.StagePositionTransition
import app.ui.control.IconButton
import app.ui.stage.BlurrableStage
import javafx.animation.Interpolator
import javafx.animation.ParallelTransition
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.effect.Effect
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import system.ResourceUtils
import system.image.ImageResolver

open class OverlayDialog(override val parent: BlurrableStage, val imageResolver: ImageResolver,
                         val enableCloseBtn: Boolean = true) : BlurrableStage() {
    private val parentBox = VBox()
    private val topSection = HBox()
    private val closeBtn = IconButton(imageResolver, "asset:x-circle", 20)

    private var dropShadowEffect: Effect? = null

    // Called when a user exit a dialog clicking the X button
    var onDialogCanceled : (() -> Unit)? = null
    var onDialogClosed : (() -> Unit)? = null

    init {
        scene = Scene(parentBox)
        scene.stylesheets.add(ResourceUtils.getResource("/css/dialog.css")!!.toURI().toString())
        this.title = "Dokey"
        this.scene = scene
        this.isAlwaysOnTop = true
        this.isResizable = false
        initStyle(StageStyle.TRANSPARENT)
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        scene.fill = Color.TRANSPARENT
        this.icons.add(Image(OverlayDialog::class.java.getResourceAsStream("/assets/icon.png")))

        maxHeight = 600.0

        // Blur the control panel stage
        Platform.runLater {
            parent.blurIn()
        }
    }

    fun initializeUI() {
        // Load the top section
        topSection.styleClass.add("top-section")
        val spacerPane = Pane()
        HBox.setHgrow(spacerPane, Priority.ALWAYS)
        val topLeftComponent = defineTopSectionComponent()
        topLeftComponent?.let { topSection.children.add(it) }
        topSection.children.add(spacerPane)

        // Avoid the top section if there are no components
        if (topLeftComponent != null || enableCloseBtn) {
            parentBox.children.add(topSection)
        }

        val contentBox = defineContentBoxComponent()
        VBox.setVgrow(contentBox, Priority.ALWAYS)
        contentBox?.let {
            it.prefWidth = 340.0
            parentBox.children.add(it)
        }

        if (enableCloseBtn) {
            topSection.children.add(closeBtn)
            // Close button event
            closeBtn.setOnAction {
                onDialogCanceled?.invoke()
                onClose()
            }
        }
    }

    open protected fun defineTopSectionComponent() : Node? = null
    open protected fun defineContentBoxComponent() : VBox? = null

    open fun onClose() {
        closeWithAnimation()
        Platform.runLater { parent.blurOut() }
    }

    fun showWithAnimation() {
        show()

        // Position the dialog over the stage
        x = parent.x + (parent.width - width) / 2
        y = parent.y + (parent.height - height) / 2

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
        transition.setOnFinished { onDialogClosed?.invoke(); close() }
        transition.play()
    }

    fun adaptHeight() {
        // Save the old height of the stage
        val oldHeight = height

        sizeToScene()

        val newHeight = height

        // Calculate how much the window should slide
        val neededMovement = (oldHeight - newHeight) / 2

        val positionTransition = StagePositionTransition(Duration.millis(200.0), this)
        positionTransition.interpolator = Interpolator.EASE_BOTH
        positionTransition.toY = this.y + neededMovement
        positionTransition.fromY = this.y
        positionTransition.play()
    }

    override fun blurIn() {
        // Backup the effect for later recovery when blurring
        dropShadowEffect = parentBox.effect

        val transition = BlurTransition(parentBox, Duration(200.0), 0.0, 10.0,
                0.0, -0.2)
        transition.play()
    }

    override fun blurOut() {
        val transition = BlurTransition(parentBox, Duration(200.0), 10.0, 0.0,
                -0.2, 0.0)
        transition.setOnFinished { parentBox.effect = dropShadowEffect }
        transition.play()
    }

    companion object {
        fun getAnchestorParent(parent: BlurrableStage) : BlurrableStage {
            var currentParent = parent
            while (currentParent.parent != null) {
                currentParent = currentParent.parent!!
            }
            return currentParent
        }
    }
}