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

    private val slides = mutableListOf(
            IntroSlide("Welcome", "Supercharge your productivity with Dokey", "asset:intro1")
    )

    private var currentSlide = 0

    init {
        val fxmlLoader = FXMLLoader(ResourceUtils.getResource("/layouts/intro.fxml").toURI().toURL())
        fxmlLoader.resources = resourceBundle
        val root : Parent = fxmlLoader.load()
        val scene = Scene(root)
        scene.stylesheets.add(ResourceUtils.getResource("/css/intro.css").toURI().toString())
        this.title = "Dokey"
        this.scene = scene
        isResizable = false
        initStyle(StageStyle.DECORATED)
        this.icons.add(ImageResolver.getImage("/assets/icon.png", 64))
        controller = fxmlLoader.getController()

        controller.statusLabel.text = "Installing..."  // TODO: i18n

        controller.buttonBox.children.addAll(prevSlideBtn, nextSlideBtn)

        loadSlide(0)
    }

    private fun loadSlide(index: Int) {
        controller.titleLabel.text = slides[index].title
        controller.descriptionLabel.text = slides[index].description
        imageResolver.loadInto(slides[index].imageId, 400, controller.imageView)
    }
}