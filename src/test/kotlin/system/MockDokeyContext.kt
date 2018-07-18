package system

import system.app_manager.MockApplicationManager
import system.model.ApplicationManager
import system.storage.StorageManagerTest

class MockDokeyContext : DokeyContext {
    private val _applicationManager = MockApplicationManager(StorageManagerTest.createMockStorageManager())

    override val applicationManager: ApplicationManager
        get() = _applicationManager

}