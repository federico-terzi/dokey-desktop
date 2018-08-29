package system.server.handlers

import json.JSONArray
import json.JSONObject
import net.model.ServiceHandler
import system.context.MobileServerContext

class ActiveApplicationListHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "active_app_list"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val activeApps = context.applicationManager.activeApplications
        val jsonArray = JSONArray()
        activeApps.forEach { app ->
            val json = JSONObject()
            json.put("name", app.name)
            json.put("path", app.executablePath)
            jsonArray.put(json)
        }
        val outputJson = JSONObject()
        outputJson.put("apps", jsonArray)
        return outputJson
    }
}