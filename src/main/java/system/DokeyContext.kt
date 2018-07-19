package system

import model.parser.ModelParser
import system.keyboard.KeyboardManager
import system.model.ApplicationManager
import system.storage.StorageManager

interface DokeyContext {
    val applicationManager : ApplicationManager
    val storageManager : StorageManager
    val modelParser : ModelParser
    val keyboardManager : KeyboardManager
}