package system.search.results

import javafx.scene.image.Image
import model.command.Command
import system.context.SearchContext
import utils.ImageResolver

class CommandResult(context: SearchContext, val command: Command) : AbstractResult(context) {
    override val title: String
        get() = command.title!!
    override val description: String?
        get() = command.description

    override val staticImage: Image?  // TODO Change
        get() = ImageResolver.getInstance().getImage(CommandResult::class.java.getResourceAsStream("/assets/google.png"), 32);

    override fun executeAction() {
        context.commandEngine.execute(command)
    }
}