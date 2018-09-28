package system.commands.general

import json.JSONObject
import system.commands.model.SimpleCommandWrapper

open class SimpleAppRelatedCommand : SimpleCommandWrapper(), AppRelatedCommand {
    private var _app: String? = null

    override var app : String?
        get() = _app
        set(value) {
            _app = value
        }

    override fun json(): JSONObject {
        val json = super.json()
        json.put("app", app)
        return json
    }

    override fun jsonExport(): JSONObject {
        val json = super.jsonExport()
        json.put("app", app)
        return json
    }

    override fun populateFromJSON(json: JSONObject) {
        super.populateFromJSON(json)
        _app = json.optString("app", null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SimpleAppRelatedCommand

        if (_app != other._app) return false

        return true
    }

    override fun contentEquals(other: Any?): Boolean {
        if (!super.contentEquals(other)) return false

        other as SimpleAppRelatedCommand

        if (_app != other._app) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (_app?.hashCode() ?: 0)
        return result
    }

    override fun contentHash(): Int {
        var result = super.contentHash()
        result = 31 * result + (_app?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return super.toString()+" _app=$app)"
    }


}