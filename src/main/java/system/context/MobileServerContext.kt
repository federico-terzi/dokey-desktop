package system.context

import model.parser.command.CommandParser
import system.applications.ApplicationManager
import system.storage.StorageManager

interface MobileServerContext {
    val applicationManager : ApplicationManager
}