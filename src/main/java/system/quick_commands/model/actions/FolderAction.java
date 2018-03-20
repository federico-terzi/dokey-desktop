package system.quick_commands.model.actions;

import json.JSONObject;
import system.quick_commands.model.DependencyResolver;

import java.util.ResourceBundle;

/**
 * This class represents the action of opening a folder;
 */
public class FolderAction extends QuickAction{
    private String path;

    public FolderAction() {
        super();
        setType(Type.FOLDER);
    }

    @Override
    public JSONObject json() {
        JSONObject json = super.json();
        json.put("path", path);
        return json;
    }

    @Override
    public void populateFromJson(JSONObject json) {
        super.populateFromJson(json);
        this.setPath(json.getString("path"));
    }

    @Override
    public boolean executeAction(DependencyResolver resolver) {
        return resolver.getApplicationManager().openFolder(path);
    }

    @Override
    public String getDisplayText(DependencyResolver resolver, ResourceBundle resourceBundle) {
        return "Open \""+path + "\"";  // TODO: i18n
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
