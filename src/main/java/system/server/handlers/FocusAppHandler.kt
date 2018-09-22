package system.server.handlers

import json.JSONArray
import json.JSONObject
import net.model.ServiceHandler
import system.context.MobileServerContext

class FocusAppHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "focus_app"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val requestedApp = body!!.getString("app")

        if (requestedApp != null) {
            context.applicationManager.openApplication(requestedApp)
        }

        return null
    }
}