package system.server.handlers

import json.JSONObject
import net.model.ServiceHandler
import system.applications.Application
import system.context.MobileServerContext

class GetSectionHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "get_section"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val requestedSectionId = body?.getString("id")
        val sectionLastEdit = body?.optLong("lastEdit", 0)

        var associatedApp : Application? = null

        // If the requested section id is "shortcut", return the section of the currently active application
        val sectionId = if (requestedSectionId == "shortcut") {
            val currentlyActiveApplication = context.applicationSwitchDaemon.currentApplication
            if (currentlyActiveApplication !=  null) {
                associatedApp = currentlyActiveApplication
                "app:${currentlyActiveApplication.executablePath}"
            }else{
                null
            }
        }else{
            requestedSectionId
        }

        if (requestedSectionId!!.startsWith("app:")) {
            val requestedAppPath = requestedSectionId.split("app:")[1]
            associatedApp = context.applicationManager.getApplication(requestedAppPath)
        }

        val section = if (sectionId != null) {
            context.sectionManager.getSection(sectionId)
        }else{
            null
        }

        val outputJson = JSONObject()

        if (section != null) {
            outputJson.put("found", true)

            // Check if the requested section is up to date in the mobile phone cache
            if (sectionLastEdit!! >= section.lastEdit!!) {
                outputJson.put("up", true)  // UP TO DATE
            }else{
                outputJson.put("up", false) // NOT UP TO DATE

                // The section must be sent
                outputJson.put("section", section.json())
            }
        } else {  // Requested section not found
            outputJson.put("found", false)

            // Add the application information
            if (associatedApp != null) {
                val appJson = JSONObject()
                appJson.put("name", associatedApp.name)
                appJson.put("path", associatedApp.executablePath)
                outputJson.put("app", appJson)
            }
        }

        return outputJson
    }
}