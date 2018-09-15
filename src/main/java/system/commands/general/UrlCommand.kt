package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand(title = "Open Web Link", iconId = "asset:link")
class UrlCommand : SimpleCommand() {
    init {
        category = "url"
    }

    var url : String?
        get() = this.value
        set(url) {
            value = url

            // Set also the icon id based on the URL
            iconId = "url:$url"
        }
}