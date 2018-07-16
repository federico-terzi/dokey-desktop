package system.app_manager

import system.model.Application

class MockApplication(name: String, executablePath: String, iconId: String?) : Application(name, executablePath, iconId) {
    override fun open(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}