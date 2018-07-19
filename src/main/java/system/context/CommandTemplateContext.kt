package system.context

import system.model.ApplicationManager
import system.storage.StorageManager

interface CommandTemplateContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
}