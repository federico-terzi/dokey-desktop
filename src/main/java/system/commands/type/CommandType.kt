package system.commands.type

import model.command.Command
import kotlin.reflect.KClass

interface CommandType {
    val iconId : String
    val title : String
    val description: String?
    val associatedCommandType : KClass<out Command>
}