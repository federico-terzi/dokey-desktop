package app.intro

import app.ui.control.IconButton
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import system.ResourceUtils
import system.image.ImageResolver
import java.util.*

class IntroStage(val resourceBundle: ResourceBundle, val imageResolver: ImageResolver) : Stage() {
    private val controller : IntroController

    private val prevSlideBtn = IconButton(imageResolver, "asset:left-circle", 36)
    private val nextSlideBtn = IconButton(imageResolver, "asset:right-circle", 36)

    init {
        val fxmlLoader = FXMLLoader(ResourceUtils.getResource("/layouts/intro.fxml").toURI().toURL())
        fxmlLoader.resources = resourceBundle
        val root : Parent = fxmlLoader.load()
        val scene = Scene(root)
        scene.stylesheets.add(ResourceUtils.getResource("/css/intro.css").toURI().toString())
        this.title = "Dokey"
        this.scene = scene
        initStyle(StageStyle.DECORATED)
        this.icons.add(ImageResolver.getImage("/assets/icon.png", 64))
        controller = fxmlLoader.getController()

        controller.statusLabel.text = "Installing..."  // TODO: i18n

        imageResolver.loadInto("asset:intro1", 400, controller.imageView)

        controller.buttonBox.children.addAll(prevSlideBtn, nextSlideBtn)
    }
}