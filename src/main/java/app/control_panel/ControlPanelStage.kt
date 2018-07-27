package app.control_panel

import app.control_panel.controllers.ControlPanelController
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import system.ResourceUtils
import system.commands.CommandManager
import system.image.ImageResolver
import system.section.SectionManager
import java.util.*

class ControlPanelStage(val resourceBundle: ResourceBundle, val sectionManager: SectionManager,
                        val commandManager: CommandManager, val imageResolver: ImageResolver) : Stage() {
    private val controller : ControlPanelController

    private var xOffset = 0.0;
    private var yOffset = 0.0;

    init {
        val fxmlLoader = FXMLLoader(ResourceUtils.getResource("/layouts/control_panel.fxml")!!.toURI().toURL())
        fxmlLoader.resources = resourceBundle
        val root = fxmlLoader.load<Parent>()
        val scene = Scene(root)
        scene.stylesheets.add(ResourceUtils.getResource("/css/control_panel.css")!!.toURI().toString())
        this.title = "Dokey Control Panel"
        this.scene = scene
        this.isAlwaysOnTop = true
        initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        this.icons.add(Image(ControlPanelStage::class.java.getResourceAsStream("/assets/icon.png")))

        controller = fxmlLoader.getController<Any>() as ControlPanelController

        root.setOnMousePressed(object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent) {
                xOffset = event.getSceneX()
                yOffset = event.getSceneY()
            }
        })

        //move around here
        root.setOnMouseDragged(object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent) {
                this@ControlPanelStage.setX(event.getScreenX() - xOffset)
                this@ControlPanelStage.setY(event.getScreenY() - yOffset)
            }
        })
    }
}