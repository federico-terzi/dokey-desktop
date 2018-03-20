package system.quick_commands.model.actions;

import json.JSONObject;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;

import java.util.ResourceBundle;

/**
 * This class represents the action of opening/focusing an application.
 */
public class ApplicationAction extends QuickAction{
    private String executablePath;

    public ApplicationAction() {
        super();
        setType(Type.APP);
    }

    @Override
    public JSONObject json() {
        JSONObject json = super.json();
        json.put("executablePath", executablePath);
        return json;
    }

    @Override
    public void populateFromJson(JSONObject json) {
        super.populateFromJson(json);
        this.setExecutablePath(json.getString("executablePath"));
    }

    @Override
    public boolean executeAction(DependencyResolver resolver) {
        return resolver.getApplicationManager().openApplication(executablePath);
    }

    @Override
    public String getDisplayText(DependencyResolver resolver, ResourceBundle resourceBundle) {
        Application application = resolver.getApplicationManager().getApplication(executablePath);

        if (application != null)
            return resourceBundle.getString("open")+" \""+application.getName() + "\"";

        return "";
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }
}
