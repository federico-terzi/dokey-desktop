package system.commands.general

import model.command.Command

interface AppRelatedCommand : Command {
    var app : String?
}