package system.commands

import model.command.Command
import model.component.CommandResolver
import system.StorageManager

class CommandManager : CommandResolver {
    val commandDir = StorageManager.getInstance().commandDir

    val commandMap = mutableMapOf<Int, Command>()

    init {

    }

    override fun getCommand(id: Int): Command? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}