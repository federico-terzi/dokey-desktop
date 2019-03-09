package system.app_manager

import system.applications.Application

class MockApplication(name: String, executablePath: String, iconId: String?) : Application(executablePath) {
    override val name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val iconPath: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun open(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}