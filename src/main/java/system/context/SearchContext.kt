package system.context

import system.bookmarks.BookmarkManager
import system.commands.CommandEngine
import system.commands.CommandManager
import system.applications.ApplicationManager
import java.util.*

interface SearchContext {
    val applicationManager : ApplicationManager
    val commandManager : CommandManager
    val commandEngine : CommandEngine
    val resourceBundle : ResourceBundle
    val bookmarkManager : BookmarkManager
}