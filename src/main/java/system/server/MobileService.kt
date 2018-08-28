package system.server

import app.MainApp
import model.component.CommandResolver
import net.DEDaemon
import net.DEManager
import net.LinkManager
import net.model.DeviceInfo
import system.ApplicationSwitchDaemon
import system.BroadcastManager
import system.applications.Application
import system.applications.ApplicationManager
import system.commands.CommandManager
import system.image.ImageResolver
import system.keyboard.KeyboardManager
import utils.SystemInfoManager
import java.io.Serializable
import java.net.Socket
import java.util.logging.Logger

class MobileService(val socket : Socket, val key : ByteArray, val commandManager: CommandManager,
                    val imageResolver: ImageResolver, keyboardManager: KeyboardManager,
                    val applicationManager: ApplicationManager, val applicationSwitchDaemon: ApplicationSwitchDaemon,
                    val onConnectionListener: DEManager.OnConnectionListener) : ApplicationSwitchDaemon.OnApplicationSwitchListener {

    var onConnectionClosed : (() -> Unit)? = null

    private val LOG = Logger.getGlobal()

    private val linkManager : LinkManager

    init {
        linkManager = LinkManager(socket, SystemInfoManager.getDeviceInfo(), MainApp.DOKEY_VERSION_NUMBER,
                MainApp.DOKEY_MOBILE_MIN_VERSION, true, key, true, commandManager,
                onConnectionListener)
    }

    fun initialize() {
        linkManager.onConnectionClosedListener = object : DEDaemon.OnConnectionClosedListener {
            override fun onConnectionClosed() {
                this@MobileService.onConnectionClosed?.invoke()
            }
        }

        applicationSwitchDaemon.addApplicationSwitchListener(this)

        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener)
    }

    fun start() {
        linkManager.startDaemon()
    }

    fun close() {
        linkManager.stopDaemon()

        // Remove the listeners
        applicationSwitchDaemon.removeApplicationSwitchListener(this)

        // Unregister the broadcast listeners
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener)
    }

    override fun onApplicationSwitch(application: Application) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val editorSectionModifiedListener = BroadcastManager.BroadcastListener {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}