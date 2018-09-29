package system.section.model

import json.JSONObject
import model.page.Page
import model.parser.page.PageParser
import model.section.ApplicationSection
import model.section.Section

open class DefaultSectionWrapper(val section: Section) : SectionWrapper {
    override var id: String?
        get() = section.id
        set(value) {section.id = value}

    override var lastEdit: Long?
        get() = section.lastEdit
        set(value) {section.lastEdit = value}

    override var name: String?
        get() = section.name
        set(value) {section.name = value}

    override var pages: MutableList<Page>?
        get() = section.pages
        set(value) {section.pages = value}

    override val type: String?
        get() = section.type

    override val appId: String?
        get() {
            section as ApplicationSection
            return section.appId
        }

    private var _deleted : Boolean = false
    override var deleted: Boolean
        get() = _deleted
        set(value) {_deleted = value}

    override fun json(): JSONObject {
        val json = section.json()
        json.put("deleted", deleted)
        return json
    }

    override fun jsonExport(): JSONObject {
        return section.jsonExport()
    }

    override fun populateFromJSON(json: JSONObject, pageParser: PageParser) {
        section.populateFromJSON(json, pageParser)
    }

    override fun populateWrapperFields(json: JSONObject) {
        deleted = json.optBoolean("deleted", false)
    }
}