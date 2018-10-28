package system.applications.MS.model

import com.sun.jna.WString
import org.xml.sax.InputSource
import system.applications.Application
import system.applications.MS.WinApplicationLib
import system.applications.MS.model.exception.ApplicationManifestNotFoundException
import system.applications.MS.model.exception.ApplicationNotFoundException
import system.applications.MS.model.exception.ManifestParsingException
import java.io.File
import java.io.StringReader
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Represents a UWP windows application installed in the system.
 * These category also includes the WindowsStore apps.
 */
class MSUWPApplication(id: String) : Application(id) {

    private lateinit var _name : String
    override val name: String
        get() = _name

    private lateinit var _iconPath : String
    override val iconPath: String?
        get() = _iconPath

    private val appId : String

    init {
        // Get the UWP application directory
        val appDir = getApplicationDirectory()
        if (appDir == null) {
            throw ApplicationNotFoundException("Cannot find UWP application directory: $id")
        }

        // Extract the application id
        appId = id.split("!").last()

        // Extract the manifest file
        val manifestFile = File(appDir, "appxmanifest.xml")
        if (!manifestFile.isFile) {
            throw ApplicationManifestNotFoundException("Cannot find manifest for UWP app: $id")
        }

        // Parse the manifest file to extract the fields
        parseManifest(appDir, manifestFile)
    }

    /**
     * Get the UWP application directory, or null if not found
     */
    private fun getApplicationDirectory() : File? {
        val pathBuffer = ByteArray(1024)
        val result = WinApplicationLib.INSTANCE.extractUWPApplicationDirectory(WString(id), pathBuffer, pathBuffer.size / 2)

        if (result > 0) {
            // Convert the path array to a string
            var pathString = String(pathBuffer, StandardCharsets.UTF_16LE)

            // Remove all the zeroes created by the conversion
            pathString = pathString.replace("\u0000", "")

            // Make sure the path is valid
            val path = File(pathString)
            if (path.isDirectory) {
                return path
            }
        }

        return null
    }

    private fun parseManifest(appDir: File, manifestFile: File) {
        // Load the manifest DOM
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(manifestFile)

        // Find the application name
        val applications = doc.getElementsByTagName("Application")
        for (i in 0 until applications.length) {
            val app = applications.item(i)
            val currentAppId = app.attributes.getNamedItem("Id").nodeValue
            val executableName = app.attributes.getNamedItem("Executable").nodeValue.split(".").first()
            // Check if the current application node is the correct one
            if (currentAppId == appId) {
                for (k in 0 until app.childNodes.length) {
                    val currentNode = app.childNodes.item(k)
                    if (currentNode.nodeName.contains("VisualElements")) {
                        val name = currentNode.attributes.getNamedItem("DisplayName").nodeValue

                        // If the name depends on a resource file, fallback to the executableName
                        if (name.startsWith("ms-resource:")) {
                            _name = executableName
                        }else{
                            _name = name
                        }
                    }
                }

                break
            }
        }

        // Find the icon
        val logoList = doc.getElementsByTagName("Logo")
        val logoPath = logoList.item(0).textContent

        // Decode the icon path
        var iconFile : File? = null
        val iconDirectory = File(appDir, logoPath).parentFile
        val iconIdentifier = logoPath.split("\\").last().split(".").first()
        for (file in iconDirectory.listFiles()) {
            if (file.name.startsWith(iconIdentifier)) {
                iconFile = file
            }
        }

        // Make sure the icon path is valid
        if (iconFile?.isFile != true) {
            throw ManifestParsingException("Cannot parse logo for app: $id")
        }

        _iconPath = iconFile.absolutePath
    }

    /**
     * Open an application.
     */
    override fun open(): Boolean {
        val runtime = Runtime.getRuntime()

        // Execute the process
        try {
            val proc = runtime.exec(arrayOf("explorer.exe", "shell:AppsFolder\\$id"))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}