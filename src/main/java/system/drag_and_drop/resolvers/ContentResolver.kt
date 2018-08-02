package system.drag_and_drop.resolvers

import javafx.scene.input.Dragboard
import model.command.Command

interface ContentResolver {
    fun resolve(dragboard: Dragboard) : Command?
}