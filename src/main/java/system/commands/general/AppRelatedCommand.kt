package system.commands.general

import system.commands.model.CommandWrapper

interface AppRelatedCommand : CommandWrapper {
    var app : String?
}