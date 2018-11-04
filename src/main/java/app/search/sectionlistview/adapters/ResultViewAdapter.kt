package app.search.sectionlistview.adapters

import app.search.sectionlistview.ListViewEntry
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import system.image.ImageResolver
import system.search.results.Result
import utils.OSValidator

/**
 * This view adapter is used to render a result view into a list cell.
 */
class ResultViewAdapter(val imageResolver: ImageResolver, val fallback : Image) : ViewAdapter {
    private val hBox = HBox()
    private val image = ImageView()
    private val title = Label()
    private val description = Label()
    private val extraText = Label()

    init {
        // Configure the style
        hBox.styleClass.add("dokey-search-result-box")
        title.styleClass.add("dokey-search-result-title")
        description.styleClass.add("dokey-search-result-description")
        extraText.styleClass.add("dokey-search-result-extra")
        image.styleClass.add("dokey-search-result-image")

        // Configure the layout
        image.fitWidth = 32.0
        image.fitHeight = 32.0
        title.textOverrun = OverrunStyle.ELLIPSIS
        description.textOverrun = OverrunStyle.ELLIPSIS

        // Add the component to the HBOX
        hBox.children.add(image)
        val vBox = VBox()
        vBox.children.add(title)
        vBox.children.add(description)
        HBox.setHgrow(vBox, Priority.ALWAYS)
        hBox.children.add(vBox)
        hBox.children.add(extraText)
    }

    private fun populateFields(result: Result) {
        // Load the text fields
        title.text = result.title
        description.text = result.description
        extraText.text = result.extra ?: ""

        imageResolver.loadInto(result.imageId, 32, image)

        // Setup drag and drop
        val dragAndDropPayload = result.generateDragAndDropPayload()
        if (dragAndDropPayload != null) {
            hBox.onDragDetected = EventHandler { event ->
                val db = hBox.startDragAndDrop(TransferMode.MOVE)

                val sp = SnapshotParameters()
                sp.fill = Color.TRANSPARENT
                val snapshot = hBox.snapshot(sp, null)

                var offsetX = 0.0
                var offsetY = 0.0

                // Workaround to compensate the position offset in the
                // Windows OS
                if (OSValidator.isWindows()) {
                    offsetX = snapshot.width / 2
                    offsetY = snapshot.height / 2
                }

                db.setDragView(snapshot, offsetX, offsetY)

                val content = ClipboardContent()
                // Put the content in the clipboard data,
                // Note: a workaround is needed when using an URL
                if (dragAndDropPayload.startsWith("http")) {
                    content.putUrl(dragAndDropPayload)
                }else{
                    content.putString(dragAndDropPayload)
                }

                db.setContent(content)

                event.consume()
            }
            hBox.onDragDone = EventHandler { event ->
                hBox.cursor = Cursor.DEFAULT

                event.consume()
            }
        }else{
            // Reset the drag and drop listeners
            hBox.onDragDone = null
            hBox.onDragDetected = null
        }
    }

    override fun isCompatible(entry: ListViewEntry) : Boolean {
        return entry.isResult
    }

    override fun updateView(entry: ListViewEntry) {
        if (entry.result != null) {
            populateFields(entry.result)
        }else{
            hBox.children.clear()
        }
    }

    override fun getView(): HBox {
        return hBox
    }
}