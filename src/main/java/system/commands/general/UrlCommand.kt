package system.commands.general

import model.command.SimpleCommand
import system.commands.annotations.RegisterCommand

@RegisterCommand
class UrlCommand : SimpleCommand() {
    init {
        category = "url"
    }

    var url : String?
        get() = this.value
        set(url) {
            value = url
        }
}