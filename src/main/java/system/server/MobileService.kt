package system.server

import app.MainApp
import model.command.Command
import model.component.CommandResolver
import net.DEDaemon
import net.DEManager
import net.LinkManager
import net.model.DeviceInfo
import net.model.ServiceHandler
import org.reflections.Reflections
import system.ApplicationSwitchDaemon
import system.BroadcastManager
import system.applications.Application
import system.applications.ApplicationManager
import system.commands.CommandEngine
import system.commands.CommandManager
import system.context.MobileServerContext
import system.image.ImageResolver
import system.keyboard.KeyboardManager
import utils.SystemInfoManager
import java.io.Serializable
import java.net.Socket
import java.util.logging.Logger

class MobileService(val socket : Socket, val key : ByteArray, val commandManager: CommandManager,
                    val context: MobileServerContext, val commandEngine: CommandEngine,
                    val imageResolver: ImageResolver, val applicationSwitchDaemon: ApplicationSwitchDaemon,
                    val onConnectionListener: DEManager.OnConnectionListener) :
        ApplicationSwitchDaemon.OnApplicationSwitchListener, LinkManager.OnCommandReceivedListener {

    var onConnectionClosed : (() -> Unit)? = null

    private val LOG = Logger.getGlobal()

    private val linkManager : LinkManager

    init {
        linkManager = LinkManager(socket, SystemInfoManager.getDeviceInfo(), MainApp.DOKEY_VERSION_NUMBER,
                MainApp.DOKEY_MOBILE_MIN_VERSION, true, key, true, commandManager,
                onConnectionListener)
    }

    fun initialize() {
        // Register the closed connection listener
        linkManager.onConnectionClosedListener = object : DEDaemon.OnConnectionClosedListener {
            override fun onConnectionClosed() {
                this@MobileService.onConnectionClosed?.invoke()
            }
        }

        // Register all handlers
        registerServiceHandlers()

        linkManager.commandListener = this

        // Register the app switch deamon
        applicationSwitchDaemon.addApplicationSwitchListener(this)

        // Register broadcast listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener)
    }

    private fun registerServiceHandlers() {
        // Load all the service handlers dynamically
        val reflections = Reflections("system.server.handlers")
        val handlers = reflections.getSubTypesOf(ServiceHandler::class.java)
        // Register all the handlers
        handlers.forEach { handlerClass ->
            val handler = handlerClass.getConstructor(MobileServerContext::class.java).newInstance(context)
            linkManager.registerServiceHandler(handler)
        }
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

    override fun onCommandReceived(command: Command) {
        commandEngine.execute(command)
    }

    private val editorSectionModifiedListener = BroadcastManager.BroadcastListener {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}