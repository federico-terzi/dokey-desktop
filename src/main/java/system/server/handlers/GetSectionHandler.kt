package system.server.handlers

import json.JSONObject
import net.model.ServiceHandler
import system.context.MobileServerContext

class GetSectionHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "get_section"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val requestedSectionId = body?.getString("id")
        val sectionLastEdit = body?.optLong("lastEdit", 0)

        val section = context.sectionManager.getSection(requestedSectionId!!)

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
        }

        return outputJson
    }
}