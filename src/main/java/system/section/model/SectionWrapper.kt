package system.section.model

import json.JSONObject
import model.section.ApplicationSection
import model.section.Section

interface SectionWrapper : Section, ApplicationSection {
    /**
     * If true, the section is disabled. It means that the section is not visible until manually re-enabled.
     */
    var deleted : Boolean

    fun populateWrapperFields(json: JSONObject)
}