package system.drag_and_drop.resolvers

import javafx.scene.input.Dragboard
import model.command.Command

class FileContentResolver : ContentResolver {
    override fun resolve(dragboard: Dragboard): Command? {
        val filePath = dragboard.files[0].absolutePath

        TODO("create file open command")
    }

}