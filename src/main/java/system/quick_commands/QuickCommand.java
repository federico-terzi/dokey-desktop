package system.quick_commands;

import json.JSONObject;
import system.quick_commands.model.actions.QuickAction;

import java.util.UUID;

/**
 * This class represents a Quick Command and has all the basic attributes.
 */
public class QuickCommand {
    private String id;  // The identifier of the quick command, a randomly generated string
    private String command;  // The command identifier, for example -> "editor"
    private QuickAction action;  // The command action.

    private String relatedAppID = null;  // A command can be optionally associated with an application

    public QuickCommand() {}

    public QuickCommand(boolean generateRandomID) {
        if (generateRandomID)
            this.id = UUID.randomUUID().toString().replace("-", "");  // Generate ID automatically
    }

    public QuickCommand(String id, String command, QuickAction action, String relatedAppID) {
        this.id = id;
        this.command = command;
        this.action = action;
        this.relatedAppID = relatedAppID;
    }

    public QuickCommand(String command, QuickAction action, String relatedAppID) {
        this.id = UUID.randomUUID().toString().replace("-", "");  // Generate ID automatically
        this.command = command;
        this.action = action;
        this.relatedAppID = relatedAppID;
    }

    /**
     * Convert the current object to a JSONObject.
     * @return the object serialized as a JSONObject.
     */
    public JSONObject json() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("command", command);
        json.put("action", action.json());

        if (relatedAppID != null) {
            json.put("relatedAppID", relatedAppID);
        }

        return json;
    }

    /**
     * Convert the passed JSONObject to a QuickCommand object.
     * @param json the JSONObject to parse.
     * @return the parsed QuickCommand object.
     */
    public static QuickCommand fromJson(JSONObject json) {
        QuickCommand quickCommand = new QuickCommand();
        quickCommand.setId(json.getString("id"));
        quickCommand.setCommand(json.getString("command"));
        quickCommand.setAction(QuickAction.fromJson(json.getJSONObject("action")));
        quickCommand.setRelatedAppID(json.optString("relatedAppID", null));
        return quickCommand;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public QuickAction getAction() {
        return action;
    }

    public void setAction(QuickAction action) {
        this.action = action;
    }

    public String getRelatedAppID() {
        return relatedAppID;
    }

    public void setRelatedAppID(String relatedAppID) {
        this.relatedAppID = relatedAppID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
