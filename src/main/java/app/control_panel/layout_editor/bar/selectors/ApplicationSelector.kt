package app.control_panel.layout_editor.bar.selectors

import model.section.ApplicationSection
import model.section.Section
import system.model.Application
import system.model.ApplicationManager

class ApplicationSelector(context: SelectorContext, section: Section) : Selector(context, section, 2) {
    val associatedApplication : Application

    override val imageId: String

    init {
        section as ApplicationSection
        val application : Application? = context.applicationManager.getApplication(section.appId)
        if (application == null) {
            throw SelectorLoadingException()
        }else{
            associatedApplication = application
        }

        imageId = "app:${associatedApplication.executablePath}"
    }


    override fun compareTo(other: Selector): Int {
        val compare = super.compareTo(other)
        return if (compare == 0) {
            other as ApplicationSelector
            associatedApplication.name.compareTo(other.associatedApplication.name)
        }else{
            compare
        }
    }
}