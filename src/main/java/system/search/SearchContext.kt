package system.search

import system.commands.CommandEngine
import system.commands.CommandManager
import system.model.ApplicationManager

interface SearchContext {
    val applicationManager : ApplicationManager
    val commandManager : CommandManager
    val commandEngine : CommandEngine
}