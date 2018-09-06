package system.context

import model.parser.command.CommandParser
import system.ApplicationSwitchDaemon
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.section.SectionManager
import system.storage.StorageManager

interface MobileServerContext {
    val applicationManager : ApplicationManager
    val commandManager: CommandManager
    val sectionManager : SectionManager
    val applicationSwitchDaemon : ApplicationSwitchDaemon
}