package system.quick_commands.model.actions;

import json.JSONObject;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;

import java.util.ResourceBundle;

/**
 * This class represents the action of opening a web link.
 */
public class WebLinkAction extends QuickAction{
    private String url;

    public WebLinkAction() {
        super();
        setType(Type.WEB_LINK);
    }

    @Override
    public JSONObject json() {
        JSONObject json = super.json();
        json.put("url", url);
        return json;
    }

    @Override
    public void populateFromJson(JSONObject json) {
        super.populateFromJson(json);
        this.setUrl(json.getString("url"));
    }

    @Override
    public boolean executeAction(DependencyResolver resolver) {
        return resolver.getApplicationManager().openWebLink(url);
    }

    @Override
    public String getDisplayText(DependencyResolver resolver, ResourceBundle resourceBundle) {
        return resourceBundle.getString("navigate_to") + " \""+url + "\"";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
