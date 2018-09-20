package system.commands.general

import system.commands.annotations.RegisterCommand
import system.commands.model.SimpleCommandWrapper

@RegisterCommand(title = "Open Web Link", iconId = "asset:link")
class UrlCommand : SimpleCommandWrapper() {
    init {
        category = "url"
    }

    var url : String?
        get() = this.value
        set(url) {
            value = url

            // Set also the icon id based on the URL if not provided
            if (iconId == null) {
                iconId = "url:$url"
            }
        }
}