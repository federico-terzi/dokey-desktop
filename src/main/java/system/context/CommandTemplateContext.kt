package system.context

import model.parser.command.CommandParser
import system.model.ApplicationManager
import system.storage.StorageManager

interface CommandTemplateContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
    val commandParser : CommandParser
}