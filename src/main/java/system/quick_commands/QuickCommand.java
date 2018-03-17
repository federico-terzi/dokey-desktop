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
    private String name;  // The name of the command, can be null
    private QuickAction action;  // The command action.

    public QuickCommand() {}

    public QuickCommand(boolean generateRandomID) {
        if (generateRandomID)
            this.id = UUID.randomUUID().toString().replace("-", "");  // Generate ID automatically
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

        if (name != null)
            json.put("name", name);

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
        quickCommand.setName(json.optString("name", null));
        quickCommand.setAction(QuickAction.fromJson(json.getJSONObject("action")));
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
