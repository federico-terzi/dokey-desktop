package app.control_panel.layout_editor.bar

import app.control_panel.layout_editor.bar.selectors.*
import app.control_panel.layout_editor.grid.SectionGrid
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import model.section.*
import system.image.ImageResolver
import system.model.ApplicationManager
import system.section.SectionManager

class SectionBar(val sectionManager: SectionManager, val applicationManager: ApplicationManager, imageResolver: ImageResolver) : ScrollPane() {
    private val selectorTypes = mapOf<Class<out Section>, Class<out Selector>>(
            LaunchpadSection::class.java to LaunchpadSelector::class.java,
            SystemSection::class.java to SystemSelector::class.java,
            DefaultApplicationSection::class.java to ApplicationSelector::class.java
    )

    val selectorContext : SelectorContext = object : SelectorContext {
        override val applicationManager: ApplicationManager
            get() = this@SectionBar.applicationManager
    }

    private val appBox = HBox()
    private val selectors : Collection<Selector>

    var onSectionClicked : ((Section) -> Unit)? = null

    init {
        this.styleClass.add("app_scroll_pane")

        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
        isPannable = true
        prefViewportHeight = 320.0

        appBox.maxHeight = Double.MAX_VALUE
        appBox.maxWidth = Double.MAX_VALUE

        content = appBox

        val sections = sectionManager.getSections()
        val selectors = mutableListOf<Selector>()
        sections.forEach { section ->
            val selectorClass = selectorTypes[section.javaClass]
            if (selectorClass != null) {
                try {
                    val selector = selectorClass.getConstructor(SelectorContext::class.java, Section::class.java)
                            .newInstance(selectorContext, section)
                    selectors.add(selector)
                }catch (ex: SelectorLoadingException) {
                    ex.printStackTrace()
                }
            }
        }
        selectors.sort()
        this.selectors = selectors

        selectors.forEach { selector ->
            val button = Button()
            val image = imageResolver.resolveImage(selector.imageId, 32)
            val imageView = ImageView(image)
            imageView.fitHeight = 32.0
            imageView.fitWidth = 32.0
            button.setGraphic(imageView)
            appBox.children.add(button)

            button.onAction = EventHandler {
                onSectionClicked?.invoke(selector.section)
            }
        }
    }
}