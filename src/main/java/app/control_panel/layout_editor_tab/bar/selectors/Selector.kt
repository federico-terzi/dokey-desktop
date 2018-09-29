package app.control_panel.layout_editor_tab.bar.selectors

import app.control_panel.layout_editor_tab.bar.SectionBar
import app.ui.control.StyledMenuItem
import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import model.section.Section

abstract class Selector(val context: SelectorContext, val sectionBar: SectionBar, val section: Section, val priority: Int,
                        val isDeletable: Boolean = true) : Comparable<Selector>, Button() {
    abstract val imageId: String

    private var _selected = false
    var selected: Boolean
        get() = _selected
        set(value) {
            if (value != _selected) {
                isCache = false

                if (value) {
                    selectorNode.opacity = 1.0

                    val transition = TranslateTransition(Duration(300.0), selectorNode)
                    transition.fromY = 10.0
                    transition.toY = 1.0
                    transition.setOnFinished { isCache = true }
                    transition.play()
                } else {
                    val transition = TranslateTransition(Duration(300.0), selectorNode)
                    transition.fromY = 1.0
                    transition.toY = 10.0
                    transition.setOnFinished { selectorNode.opacity = 0.0; isCache = true }
                    transition.play()
                }
            }

            _selected = value
        }

    private val selectorNode: Rectangle = Rectangle()

    init {
        styleClass.add("app-scroll-pane-button")
    }

    fun initialize() {
        val image = context.imageResolver.resolveImage(imageId, 40)
        val imageView = ImageView(image)
        imageView.fitHeight = 40.0
        imageView.fitWidth = 40.0

//        selectorNode.content = "M25 48 L42 48 L33 40 Z"
        selectorNode.width = 48.0
        selectorNode.height = 4.0
        selectorNode.arcHeight = 5.0
        selectorNode.arcWidth = 5.0
        selectorNode.fill = Color.WHITE
        selectorNode.opacity = 0.0

        val vBox = VBox()
        vBox.styleClass.add("app-selector-vbox")
        vBox.alignment = Pos.CENTER
        vBox.children.addAll(imageView, selectorNode)
        graphic = vBox

        setupContextMenu()

        isCache = true
        cacheHint = CacheHint.SPEED
    }

    private fun setupContextMenu() {
        val contextMenu = ContextMenu()
        val exportItem : MenuItem = StyledMenuItem("/assets/external-link.png", "Export")  // TODO: i18n
        exportItem.setOnAction {
            sectionBar.onExportRequest?.invoke(section)
        }

        val resetMenuItem = StyledMenuItem("/assets/repeat.png", "Reset")  // TODO: i18n
        resetMenuItem.setOnAction {
            sectionBar.onResetRequest?.invoke(section)
        }

        contextMenu.items.addAll(exportItem, SeparatorMenuItem(), resetMenuItem)

        if (isDeletable) {
            val deleteMenuItem = StyledMenuItem("/assets/delete.png", "Delete")  // TODO: i18n
            deleteMenuItem.setOnAction {
                sectionBar.onDeleteRequest?.invoke(section)
            }

            contextMenu.items.add(deleteMenuItem)
        }

        // Load specific entries
        val specificEntries = getSpecificContextMenuEntries()

        if (specificEntries.isNotEmpty()) {
            contextMenu.items.add(SeparatorMenuItem())
            contextMenu.items.addAll(specificEntries)
        }

        this.contextMenu = contextMenu
    }

    protected open fun getSpecificContextMenuEntries(): List<MenuItem> {
        return listOf()
    }

    override fun compareTo(other: Selector): Int {
        return priority.compareTo(other.priority)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Selector

        if (section != other.section) return false

        return true
    }

    override fun hashCode(): Int {
        return section.hashCode()
    }


}