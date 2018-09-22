package system.server

import app.MainApp
import json.JSONObject
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
import system.section.SectionManager
import utils.SystemInfoManager
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.net.Socket
import java.util.logging.Logger

class MobileService(val socket : Socket, val key : ByteArray, val commandManager: CommandManager,
                    val context: MobileServerContext, val commandEngine: CommandEngine,
                    val imageResolver: ImageResolver, val applicationSwitchDaemon: ApplicationSwitchDaemon,
                    val sectionManager: SectionManager,
                    val onConnectionListener: DEManager.OnConnectionListener) :
        ApplicationSwitchDaemon.OnApplicationSwitchListener, LinkManager.OnCommandReceivedListener, LinkManager.OnImageRequestListener {

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
        linkManager.setImageRequestListener(this)

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

        // Close the socket
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Remove the listeners
        applicationSwitchDaemon.removeApplicationSwitchListener(this)

        // Unregister the broadcast listeners
        BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, editorSectionModifiedListener)
    }

    override fun onApplicationSwitch(application: Application) {
        // Check if the application has an associated section
        val associatedSection = sectionManager.getSection("app:${application.executablePath}")

        val requestJson = JSONObject()
        requestJson.put("appName", application.name)

        if (associatedSection != null) {
            requestJson.put("sectionId", associatedSection.id)
            requestJson.put("lastEdit", associatedSection.lastEdit)
        }

        linkManager.requestService("app_switch", requestJson, null)
    }

    override fun onCommandReceived(command: Command) {
        commandEngine.execute(command)
    }

    override fun onImageRequestReceived(imageIdentifier: String): File? {
        return imageResolver.resolveImageFile(imageIdentifier)
    }

    private val editorSectionModifiedListener = BroadcastManager.BroadcastListener {
        val sectionId = it as String?
        if (sectionId != null) {
            val section = sectionManager.getSection(sectionId)
            if (section != null) {
                linkManager.requestService("section_edit", section.json(), null)
            }
        }
    }
}