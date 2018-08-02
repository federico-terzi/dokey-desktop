package system.context

import system.model.ApplicationManager
import system.storage.StorageManager

interface ImageSourceContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
}