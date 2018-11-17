package app.control_panel.layout_editor_tab.grid.model

import json.JSONArray
import json.JSONObject
import model.component.Component
import model.page.Page
import model.parser.component.ComponentParser
import model.section.Section

data class ComponentReference(val components: List<Component>, val pageIndex: Int, val sectionId: String) {
    fun json() : JSONObject {
        val payload = JSONObject()
        val componentsArray = JSONArray()
        components.forEach {
            componentsArray.put(it.json())
        }
        payload.put("components", componentsArray)
        payload.put("pageIndex", pageIndex)
        payload.put("sectionId", sectionId)
        return payload
    }

    companion object {
        fun fromJson(componentParser: ComponentParser, json: JSONObject) : ComponentReference {
            val componentsArray = json.getJSONArray("components")
            val components = mutableListOf<Component>()
            for (jcomp in componentsArray) {
                jcomp as JSONObject
                val component = componentParser.fromJSON(jcomp)
                components.add(component)
            }

            return ComponentReference(components, json.getInt("pageIndex"), json.getString("sectionId"))
        }
    }
}