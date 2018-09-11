package app.control_panel.layout_editor_tab.bar.selectors

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import model.section.ApplicationSection
import model.section.Section
import system.applications.Application
import system.drag_and_drop.DNDCommandProcessor
import utils.OSValidator

class ApplicationSelector(context: SelectorContext, section: Section) : Selector(context, section, 2) {
    val associatedApplication : Application

    override val imageId: String

    init {
        section as ApplicationSection
        val application : Application? = context.applicationManager.getApplication(section.appId)
        if (application == null) {
            throw SelectorLoadingException()
        }else{
            associatedApplication = application
        }

        imageId = "app:${associatedApplication.executablePath}"

        // Setup drag and drop
        val associatedOpenCommand = context.commandManager.getAppOpenCommand(application.executablePath)
        if (associatedOpenCommand != null) {
            val dragAndDropPayload = "${DNDCommandProcessor.dragAndDropPrefix}:command:${associatedOpenCommand.id}"
            onDragDetected = EventHandler { event ->
                val db = startDragAndDrop(TransferMode.MOVE)

                val sp = SnapshotParameters()
                sp.fill = Color.TRANSPARENT
                val snapshot = snapshot(sp, null)

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


    override fun compareTo(other: Selector): Int {
        val compare = super.compareTo(other)
        return if (compare == 0) {
            other as ApplicationSelector
            associatedApplication.name.compareTo(other.associatedApplication.name)
        }else{
            compare
        }
    }
}