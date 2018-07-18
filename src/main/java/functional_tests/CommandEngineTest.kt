package functional_tests

import system.DokeyContext
import system.MS.MSApplicationManager
import system.commands.CommandEngine
import system.commands.general.FolderOpenCommand
import system.model.ApplicationManager
import system.startup.MSStartupManager
import system.storage.BaseDirectoryStorageManager
import system.storage.StorageManager

fun main(args : Array<String>) {
    val commandEngineTest = CommandEngineTest()
    commandEngineTest.test()
}

class CommandEngineTest {
    fun test() {
        val storageManager = StorageManager.getDefault()
        val startupManager = MSStartupManager(storageManager)
        val appManager : ApplicationManager = MSApplicationManager(storageManager, startupManager)
        appManager.loadApplications(object : ApplicationManager.OnLoadApplicationsListener {
            override fun onPreloadUpdate(applicationName: String?, current: Int, total: Int) {
                println("Preload $applicationName")
            }

            override fun onProgressUpdate(applicationName: String?, iconPath: String?, current: Int, total: Int) {
                println("Progress $applicationName")
            }

            override fun onApplicationsLoaded() {
                val commandEngine = CommandEngine(object : DokeyContext {
                    override val applicationManager: ApplicationManager
                        get() = appManager

                })
                val folderCommand = FolderOpenCommand()
                folderCommand.folder = "D:\\Dropbox"
                commandEngine.execute(folderCommand)
            }
        })
    }
}