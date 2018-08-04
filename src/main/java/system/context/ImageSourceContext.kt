package system.context

import system.applications.ApplicationManager
import system.storage.StorageManager

interface ImageSourceContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
}