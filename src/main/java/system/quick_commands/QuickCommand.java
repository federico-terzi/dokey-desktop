package system.quick_commands;

import json.JSONArray;
import json.JSONObject;
import section.model.Item;

/**
 * This class represents a Quick Command and has all the basic attributes.
 */
public class QuickCommand {
    private String command;  // The command identifier, for example -> "editor"
    private String description = null;
    private Item item;  // The item corresponding to the action.

    private String relatedAppID = null;  // A command can be optionally associated with an application

    /**
     * Convert the current object to a JSONObject.
     * @return the object serialized as a JSONObject.
     */
    public JSONObject json() {
        JSONObject json = new JSONObject();
        json.put("command", command);
        json.put("item", item.json());

        if (description != null) {
            json.put("description", description);
        }
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
        quickCommand.setCommand(json.getString("command"));
        quickCommand.setDescription(json.optString("description", null));
        quickCommand.setItem(Item.fromJson(json.getJSONObject("item")));
        quickCommand.setRelatedAppID(json.optString("relatedAppID", null));
        return quickCommand;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getRelatedAppID() {
        return relatedAppID;
    }

    public void setRelatedAppID(String relatedAppID) {
        this.relatedAppID = relatedAppID;
    }
}
