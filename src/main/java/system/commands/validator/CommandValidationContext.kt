package system.commands.validator

import system.ApplicationPathResolver
import system.applications.ApplicationManager

interface CommandValidationContext {
    val applicationPathResolver : ApplicationPathResolver
    val applicationManager: ApplicationManager
}