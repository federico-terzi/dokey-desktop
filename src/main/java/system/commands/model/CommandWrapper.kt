package system.commands.model

import json.JSONObject
import model.command.Command

interface CommandWrapper : Command {
    /**
     * The author of the current command, usually it can have the values "user" or "auto" if the
     * command was created by a user or automatically generated from a template respectively.
     */
    var author : String

    /**
     * If true, the command cannot be modified by the user. Usually used for commands automatically
     * generated from templates so the user cannot modify it.
     */
    var locked : Boolean

    /**
     * If true, the command is not visible from the command tab, but can still be used in dokey search
     * or in layouts. Used for those automatically generated commands that may clutter the command tab
     * and give small value to the user. ( As the AppOpenCommand )
     */
    var implicit : Boolean

    /**
     * If true, the command is disabled. It means that the command is not visible in the dokey search,
     * command tab or anywhere else.
     */
    var deleted : Boolean
}