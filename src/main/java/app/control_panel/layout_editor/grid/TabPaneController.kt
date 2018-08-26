package app.control_panel.layout_editor.grid

import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.layout.HBox
import javafx.util.Duration
import system.image.ImageResolver


class TabPaneController(private val tabPane: TabPane, private val tabContent: Map<Tab, Node>,
                        private val listener: OnTabListener?)
    : HBox() {

    private var isTransitioning = false

    init {
        this.styleClass.add("tab-pane-controller")
        this.alignment = Pos.CENTER

        // Transition animation
        tabPane.selectionModel
                .selectedItemProperty()
                .addListener { _, oldTab, newTab ->
                    isTransitioning = true

                    val direction: Int

                    // Determine the slide direction
                    if (tabPane.tabs.indexOf(newTab) > tabPane.tabs.indexOf(oldTab)) {
                        direction = -1
                    } else {
                        direction = 1
                    }

                    oldTab.content = null
                    val oldContent = tabContent[oldTab]
                    val newContent = tabContent[newTab]

                    newTab.content = oldContent
                    val slideOut = TranslateTransition(
                            Duration.seconds(SLIDE_DURATION), oldContent)
                    slideOut.byX = direction * tabPane.width
                    val fadeOut = FadeTransition(Duration.seconds(SLIDE_DURATION), oldContent)
                    fadeOut.toValue = 0.0
                    fadeOut.fromValue = 1.0

                    val slideIn = TranslateTransition(
                            Duration.seconds(SLIDE_DURATION), newContent)
                    slideIn.fromX = -direction * tabPane.width
                    slideIn.toX = 0.0
                    val fadeIn = FadeTransition(Duration.seconds(SLIDE_DURATION), newContent)
                    fadeIn.toValue = 1.0
                    fadeIn.fromValue = 0.0

                    slideOut.setOnFinished { event -> newTab.content = newContent }

                    val crossFade = SequentialTransition(
                            ParallelTransition(slideOut, fadeOut),
                            ParallelTransition(slideIn, fadeIn))

                    crossFade.setOnFinished { isTransitioning = false }

                    crossFade.play()
                }

        render()
    }

    fun render() {
        this.children.clear()

        var index = 0
        for (tab in tabPane.tabs) {
            val button = Button()
            val finalIndex = index
            button.setOnAction(object : EventHandler<ActionEvent> {
                override fun handle(event: ActionEvent) {
                    if (listener != null) {
                        listener.onTabSelected(finalIndex)
                        render()
                    }
                }
            })
            val image = ImageResolver.getImage("/assets/circle_full.png", 8)
            val imageView = ImageView(image)
            imageView.fitHeight = 8.0
            imageView.fitWidth = 8.0
            if (tab.isSelected) {
                imageView.getStyleClass().add("dot-selected")
            } else {
                imageView.getStyleClass().remove("dot-selected")
            }
            button.setGraphic(imageView)
            button.setContextMenu(tab.contextMenu)

            // Handle the drag and drop focus switch
            button.setOnDragEntered(object : EventHandler<DragEvent> {
                override fun handle(event: DragEvent) {
                    tabPane.selectionModel.select(tab)
                    render()
                }
            })

            this.children.add(button)
            index++
        }

        // Add button
        val addBtn = Button()
        addBtn.setOnAction(object : EventHandler<ActionEvent> {
            override fun handle(event: ActionEvent) {
                if (listener != null) {
                    listener.onAddTab()
                    render()
                }
            }
        })
        val image = ImageResolver.getImage("/assets/circle_full.png", 8)
        val imageView = ImageView(image)
        imageView.fitWidth = 8.0
        imageView.fitHeight = 8.0
        imageView.styleClass.add("add-page-button")
        addBtn.setGraphic(imageView)
        this.children.add(addBtn)
    }

    fun selectTab(index: Int) {
        if (!isTransitioning) {
            tabPane.selectionModel.select(index)
        }
    }

    interface OnTabListener {
        fun onTabSelected(index: Int)
        fun onAddTab()
    }

    companion object {
        val SLIDE_DURATION = 0.1
    }
}
