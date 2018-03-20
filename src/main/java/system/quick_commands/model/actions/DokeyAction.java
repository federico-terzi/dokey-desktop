package system.quick_commands.model.actions;

import json.JSONObject;
import system.BroadcastManager;
import system.quick_commands.model.DependencyResolver;

import java.util.ResourceBundle;

/**
 * This class represents a specific Dokey action.
 */
public class DokeyAction extends QuickAction{
    private DokeyActionType dokeyActionType;

    public DokeyAction() {
        super();
        setType(Type.DOKEY);
    }

    @Override
    public JSONObject json() {
        JSONObject json = super.json();
        json.put("dokeyActionType", dokeyActionType.name());
        return json;
    }

    @Override
    public void populateFromJson(JSONObject json) {
        super.populateFromJson(json);
        this.setDokeyActionType(DokeyActionType.valueOf(json.getString("dokeyActionType")));
    }

    @Override
    public boolean executeAction(DependencyResolver resolver) {
        switch (dokeyActionType) {
            case OPEN_EDITOR:
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, null);
                return true;
            case OPEN_SETTINGS:
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_SETTINGS_REQUEST_EVENT, null);
                return true;
            case OPEN_COMMANDS:
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_COMMANDS_REQUEST_EVENT, null);
                return true;
        }

        return false;
    }

    @Override
    public String getDisplayText(DependencyResolver resolver, ResourceBundle resourceBundle) {
        return dokeyActionType.getDescription(resourceBundle);
    }

    /**
     * These are the types of Dokey actions that can be performed.
     */
    public enum DokeyActionType {
        OPEN_EDITOR("open_editor"),
        OPEN_SETTINGS("open_settings"),
        OPEN_COMMANDS("open_commands");

        private String descriptionId;  // This field indicates a resource bundle key to get the displayText
                                       // in the correct language.

        DokeyActionType(String descriptionId) {
            this.descriptionId = descriptionId;
        }

        public String getDescriptionId() {
            return descriptionId;
        }

        public String getDescription(ResourceBundle resourceBundle) {
            return resourceBundle.getString(descriptionId);
        }
    }

    public DokeyActionType getDokeyActionType() {
        return dokeyActionType;
    }

    public void setDokeyActionType(DokeyActionType dokeyActionType) {
        this.dokeyActionType = dokeyActionType;
    }
}
