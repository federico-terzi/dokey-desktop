package app.intro

import app.MainApp
import app.ui.control.IconButton
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.effect.Effect
import javafx.scene.image.ImageView
import javafx.stage.Stage
import javafx.stage.StageStyle
import system.ResourceUtils
import system.image.ImageResolver
import java.net.URI
import java.util.*

class IntroStage(val resourceBundle: ResourceBundle, val imageResolver: ImageResolver) : Stage() {
    private val controller : IntroController

    private val prevSlideBtn = IconButton(imageResolver, "asset:left-circle", 36)
    private val nextSlideBtn = IconButton(imageResolver, "asset:right-circle", 36)
    private val launchBtn = Button("Start")  // TODO: i18n

    private val googlePlayBtn = Button()

    private val slides = mutableListOf(  // TODO: i18n
            IntroSlide("Welcome", "Supercharge your productivity with Dokey", "asset:intro1"),
            IntroSlide("Customize", "Get hundreds of fully customizable built-in interfaces", "asset:intro2"),
            IntroSlide("Search", "Boost your workflow with the powerful all-in-one Search bar", "asset:intro3"),
            IntroSlide("Everywere", "A platform for all your devices", "asset:intro4"),
            IntroSlide("Start Now", "1. Download Dokey App\n2. Scan the QRCode from the app.", "asset:intro5")
    )

    private var currentSlide = 0

    var progress: Double
        get() = controller.progressBar.progress
        set(value) {
            controller.progressBar.progress = value

            if (value > 99) {
                launchBtn.isDisable = false
                controller.statusLabel.text = "Ready"  // TODO: i18n
            }else{
                launchBtn.isDisable = true
            }
        }

    var onIntroCompleted : (() -> Unit)? = null

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

        // Setup google play button
        run {
            val googlePlayImageView = ImageView()
            googlePlayImageView.fitWidth = 150.0
            googlePlayImageView.fitHeight = 47.0
            googlePlayImageView.image = ImageResolver.getImage(IntroStage::class.java.getResourceAsStream("/assets/google-play-btn.png"), 300)
            googlePlayBtn.graphic = googlePlayImageView
            googlePlayBtn.styleClass.add("google-play-btn")
            googlePlayBtn.setOnAction {
                java.awt.Desktop.getDesktop().browse(URI(MainApp.PLAYSTORE_URL));
            }
            controller.contentBox.children.add(googlePlayBtn)
        }

        // Setup launch button
        run {
            launchBtn.styleClass.add("launch-btn")
            launchBtn.isVisible = false
        }

        controller.buttonBox.children.addAll(launchBtn, prevSlideBtn, nextSlideBtn)

        // Previous/Next button actions
        prevSlideBtn.setOnAction {
            if (currentSlide > 0) {
                currentSlide--
                loadSlide(currentSlide)
            }
        }
        nextSlideBtn.setOnAction {
            if (currentSlide < (slides.size - 1)) {
                currentSlide++
                loadSlide(currentSlide)
            }
        }
        launchBtn.setOnAction {
            onIntroCompleted?.invoke()
            close()
        }

        loadSlide(0)
    }

    /**
     * Render the interface based on the current slide
     */
    private fun loadSlide(index: Int) {
        controller.titleLabel.text = slides[index].title
        controller.descriptionLabel.text = slides[index].description
        imageResolver.loadInto(slides[index].imageId, 400, controller.imageView)

        if (index == 0) {
            prevSlideBtn.isManaged = false
            prevSlideBtn.isVisible = false
            nextSlideBtn.isManaged = true
            nextSlideBtn.isVisible = true

            googlePlayBtn.isVisible = false
        }else if (index == (slides.size - 1)) {
            prevSlideBtn.isManaged = false
            prevSlideBtn.isVisible = false
            nextSlideBtn.isManaged = false
            nextSlideBtn.isVisible = false

            googlePlayBtn.isVisible = true
            launchBtn.isVisible = true
        }else{
            prevSlideBtn.isManaged = true
            prevSlideBtn.isVisible = true
            nextSlideBtn.isManaged = true
            nextSlideBtn.isVisible = true

            googlePlayBtn.isVisible = false
        }

        loadTabIndicators(index)
    }

    /**
     * Render the balls that mark the current tab
     */
    private fun loadTabIndicators(index: Int) {
        controller.ballBox.children.clear()

        (0 until slides.size).forEach { i ->
            val imageView = ImageView()
            imageView.fitWidth = 12.0
            imageView.fitHeight = 12.0
            imageView.image = imageResolver.resolveImage("asset:circle_full", 12)

            imageView.styleClass.add("ball")

            if (i == index) {
                imageView.styleClass.add("ball-selected")
            }

            controller.ballBox.children.add(imageView)
        }
    }
}