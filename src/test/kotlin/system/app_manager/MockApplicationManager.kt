package system.app_manager

import system.applications.Application
import system.applications.ApplicationManager
import system.storage.StorageManager
import java.io.File

class MockApplicationManager(storageManager: StorageManager) : ApplicationManager(storageManager) {
    override fun getActivePID(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveApplication(): Application {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveApplications(): MutableList<Application> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadApplications(listener: OnLoadApplicationsListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getApplicationList(): MutableList<Application> {
        return mutableListOf(
                MockApplication("Chrome", "C:\\Programs\\chrome.exe", null),
                MockApplication("Firefox", "C:\\Files\\firefox.exe", "firefox")
        )
    }

    override fun getApplication(applicationId: String?): Application {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openApplication(applicationId: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getApplicationIcon(applicationId: String?): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun open(filePath: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openWebLink(url: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openTerminalWithCommand(command: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun focusDokey(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun focusSearch(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}