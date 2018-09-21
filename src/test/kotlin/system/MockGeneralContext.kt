/*
package system

import model.parser.DesktopModelParser
import model.parser.ModelParser
import system.app_manager.MockApplicationManager
import system.commands.MockCommandResolver
import system.context.GeneralContext
import system.keyboard.KeyboardManager
import system.keyboard.MockKeyboardManager
import system.applications.ApplicationManager
import system.storage.StorageManager
import system.storage.StorageManagerTest

class MockGeneralContext : GeneralContext {
    private val _applicationManager = MockApplicationManager(StorageManagerTest.createMockStorageManager())
    private val _storageManager = StorageManagerTest.createMockStorageManager()
    private val _modelParser = DesktopModelParser(MockCommandResolver(), listOf())
    private val _keyboardManager = MockKeyboardManager()

    override val applicationManager: ApplicationManager
        get() = _applicationManager
    override val storageManager: StorageManager
        get() = _storageManager
    override val modelParser: ModelParser
        get() = _modelParser
    override val keyboardManager: KeyboardManager
        get() = _keyboardManager
}*/
