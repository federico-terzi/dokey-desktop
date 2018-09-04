package system.server.handlers

import json.JSONArray
import json.JSONObject
import net.model.ServiceHandler
import system.context.MobileServerContext

class GetCommandHandler(val context: MobileServerContext) : ServiceHandler {
    override val targetType: String = "get_command"

    override fun onServiceRequest(body: JSONObject?): JSONObject? {
        val requestedCommandId = body?.getInt("id")
        val commandLastEdit = body?.optLong("lastEdit", 0)

        val command = context.commandManager.getCommand(requestedCommandId!!)

        val outputJson = JSONObject()

        if (command != null) {
            outputJson.put("found", true)

            // Check if the requested command is up to date in the mobile phone cache
            if (commandLastEdit!! >= command.lastEdit!!) {
                outputJson.put("up", true)  // UP TO DATE
            }else{
                outputJson.put("up", false) // NOT UP TO DATE

                // The command must be sent
                outputJson.put("command", command.json())
            }
        } else {  // Requested command not found
            outputJson.put("found", false)
        }

        return outputJson
    }
}