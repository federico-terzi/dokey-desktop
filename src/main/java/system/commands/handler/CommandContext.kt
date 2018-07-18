package system.commands.handler

import system.model.ApplicationManager

interface CommandContext {
    val applicationManager : ApplicationManager
}