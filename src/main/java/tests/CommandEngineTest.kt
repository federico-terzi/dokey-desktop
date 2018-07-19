package tests

fun main(args : Array<String>) {
    val commandEngineTest = CommandEngineTest()
    commandEngineTest.test()
}

class CommandEngineTest {
    fun test() {
//        val storageManager = StorageManager.getDefault()
//        val startupManager = MSStartupManager(storageManager)
//        val appManager : ApplicationManager = MSApplicationManager(storageManager, startupManager)
//        appManager.loadApplications(object : ApplicationManager.OnLoadApplicationsListener {
//            override fun onPreloadUpdate(applicationName: String?, current: Int, total: Int) {
//                println("Preload $applicationName")
//            }
//
//            override fun onProgressUpdate(applicationName: String?, iconPath: String?, current: Int, total: Int) {
//                println("Progress $applicationName")
//            }
//
//            override fun onApplicationsLoaded() {
//                val commandEngine = CommandEngine(MockDokeyContext())
//                val folderCommand = FolderOpenCommand()
//                folderCommand.folder = "D:\\Dropbox"
//                commandEngine.execute(folderCommand)
//            }
//        })
    }
}