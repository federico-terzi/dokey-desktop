package system.commands.model

import json.JSONObject
import model.command.SimpleCommand

open class SimpleCommandWrapper : SimpleCommand(), CommandWrapper {
    override var author: String = "auto"
    override var locked: Boolean = false
    override var implicit: Boolean = false

    override fun json(): JSONObject {
        val json = super.json()
        json.put("author", author)
        json.put("locked", locked)
        json.put("implicit", implicit)
        return json
    }

    override fun jsonExport(): JSONObject {
        val json = super.jsonExport()
        json.put("locked", locked)
        json.put("implicit", implicit)
        return json
    }

    override fun populateFromJSON(json: JSONObject) {
        super.populateFromJSON(json)

        if (json.has("author")) {
            author = json.getString("author")
        }

        if (json.has("locked")) {
            locked = json.getBoolean("locked")
        }

        if (json.has("implicit")) {
            implicit = json.getBoolean("implicit")
        }
    }

    override fun toString(): String {
        return super.toString()+" author=$author, locked=$locked, implicit=$implicit )"
    }
}