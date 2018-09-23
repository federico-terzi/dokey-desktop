package system.server.handlers

import json.JSONArray
import json.JSONObject
import net.model.ServiceHandler
import system.BroadcastManager
import system.context.MobileServerContext

class RequestEditorHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "request_editor"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val requestedSectionId = body?.optString("section_id", null)

        // If the section doesn't already exists, create one.
        if (requestedSectionId != null && context.sectionManager.getSection(requestedSectionId) == null) {
            if (requestedSectionId.startsWith("app:")) {
                val requestedApp = requestedSectionId.split("app:")[1]
                val application = context.applicationManager.getApplication(requestedApp)
                if (application != null) {
                    context.sectionManager.createSectionForApp(application)
                }
            }
        }

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, requestedSectionId)

        return null
    }
}