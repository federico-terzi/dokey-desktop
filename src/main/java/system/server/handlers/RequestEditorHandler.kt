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

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, requestedSectionId)

        return null
    }
}