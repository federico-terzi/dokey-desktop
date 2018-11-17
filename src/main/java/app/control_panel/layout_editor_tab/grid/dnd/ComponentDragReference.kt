package app.control_panel.layout_editor_tab.grid.dnd

import json.JSONArray
import json.JSONObject
import model.component.Component
import model.parser.component.ComponentParser

data class ComponentDragReference(val components: List<Component>, val pageIndex: Int, val sectionId: String,
                                  val dragX: Int, val dragY: Int) {
    fun json() : JSONObject {
        val payload = JSONObject()
        val componentsArray = JSONArray()
        components.forEach {
            componentsArray.put(it.json())
        }
        payload.put("components", componentsArray)
        payload.put("pageIndex", pageIndex)
        payload.put("sectionId", sectionId)
        payload.put("x", dragX)
        payload.put("y", dragY)
        return payload
    }

    companion object {
        fun fromJson(componentParser: ComponentParser, json: JSONObject) : ComponentDragReference {
            val componentsArray = json.getJSONArray("components")
            val components = mutableListOf<Component>()
            for (jcomp in componentsArray) {
                jcomp as JSONObject
                val component = componentParser.fromJSON(jcomp)
                components.add(component)
            }

            return ComponentDragReference(components, json.getInt("pageIndex"), json.getString("sectionId"),
                    json.getInt("x"), json.getInt("y"))
        }
    }
}