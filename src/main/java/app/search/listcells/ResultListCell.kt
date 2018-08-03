package app.search.listcells

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import system.image.ImageResolver
import system.search.results.AbstractResult
import utils.OSValidator

class ResultListCell(private val fallback: Image?, private val imageResolver: ImageResolver) : ListCell<AbstractResult>() {

    private val hBox = HBox()
    private val image = ImageView()
    private val title = Label()
    private val description = Label()

    init {
        prefHeight = ROW_HEIGHT

        configureGrid()
        addControlsToGrid()
    }

    private fun configureGrid() {
        hBox.styleClass.add("dokey-search-result-box")
        title.styleClass.add("dokey-search-result-title")
        description.styleClass.add("dokey-search-result-description")
        image.styleClass.add("dokey-search-result-image")

        image.fitWidth = 32.0
        image.fitHeight = 32.0
    }

    private fun addControlsToGrid() {
        hBox.children.add(image)

        val vBox = VBox()
        vBox.children.add(title)
        vBox.children.add(description)
        hBox.children.add(vBox)
    }

    private fun clearContent() {
        text = null
        graphic = null
    }

    private fun addContent(result: AbstractResult) {
        text = null

        title.text = result.title
        description.text = result.description

        image.image = fallback

        imageResolver.resolveImageAsync(result.imageId!!, 32) {resolvedImage, externalThread ->
            if (resolvedImage != null) {
                if (externalThread) {
                    Platform.runLater {
                        image.image = resolvedImage
                    }
                }else{
                    image.image = resolvedImage
                }
            }
        }

        graphic = hBox

        // Setup drag and drop
        val dragAndDropPayload = result.generateDragAndDropPayload()
        if (dragAndDropPayload != null) {
            onDragDetected = EventHandler { event ->
                val db = startDragAndDrop(TransferMode.MOVE)

                val sp = SnapshotParameters()
                sp.fill = Color.TRANSPARENT
                val snapshot = snapshot(sp, null)

                var offsetX = 0.0
                var offsetY = 0.0

                if (OSValidator.isWindows()) {
                    offsetX = snapshot.width / 2
                    offsetY = snapshot.height / 2
                }

                db.setDragView(snapshot, offsetX, offsetY)

                val content = ClipboardContent()
                content.putString(dragAndDropPayload)
                db.setContent(content)

                event.consume()
            }
            onDragDone = EventHandler { event ->
                cursor = Cursor.DEFAULT

                event.consume()
            }
        }
    }

    override fun updateItem(result: AbstractResult?, empty: Boolean) {
        super.updateItem(result, empty)
        if (empty) {
            clearContent()
        } else {
            addContent(result!!)
        }
    }

    companion object {
        val ROW_HEIGHT = 55.0
    }
}
