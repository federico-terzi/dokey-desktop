package system

import model.parser.ModelParser
import system.model.ApplicationManager
import system.storage.StorageManager

interface DokeyContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
    val modelParser : ModelParser
}