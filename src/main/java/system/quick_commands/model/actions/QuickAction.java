package system.quick_commands.model.actions;

import json.JSONObject;
import system.quick_commands.model.DependencyResolver;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

/**
 * This class represents an action relative to a quick command
 */
public abstract class QuickAction {
    private Type type;

    /**
     * Called to execute the corresponding quick action.
     * @param resolver a DependencyResolver to obtain a reference to all the managers
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean executeAction(DependencyResolver resolver);

    /**
     * @param resolver a DependencyResolver to obtain a reference to all the managers
     * @param resourceBundle the ResourceBundle used for the i18n.
     * @return a human readable text describing what the action does.
     */
    public abstract String getDisplayText(DependencyResolver resolver, ResourceBundle resourceBundle);

    /**
     * Convert the current object to a JSONObject.
     * @return the object serialized as a JSONObject.
     */
    public JSONObject json() {
        JSONObject json = new JSONObject();
        json.put("type", getType().name());
        return json;
    }

    /**
     * Populate the current object parsing the values from the passed
     * JSONObject.
     * @param jsonItem the JSONObject to parse.
     */
    public void populateFromJson(JSONObject jsonItem) {
        this.setType(Type.valueOf(jsonItem.getString("type")));
    }

    /**
     * Convert the passed JSONObject to a subclass of QuickAction,
     * corresponding to the type read in the json.
     * @param json the JSONObject to parse.
     * @return the parsed subclass of QuickAction.
     */
    public static QuickAction fromJson(JSONObject json) {
        // Read the type string
        String type = json.getString("type");


        try {
            // Get the corresponding enum
            Type actionType = Type.valueOf(type);

            // Get an instance of the correct QuickAction subclass
            QuickAction action = actionType.getActionClass().getConstructor().newInstance();

            // Populate the action with the json data and return it
            action.populateFromJson(json);

            return action;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Types of Quick Actions
     */
    public enum Type {
        APP(ApplicationAction.class),
        WEB_LINK(WebLinkAction.class);

        private Class<? extends QuickAction> actionClass;

        Type(Class<? extends QuickAction> actionClass) {
            this.actionClass = actionClass;
        }

        public Class<? extends QuickAction> getActionClass() {
            return actionClass;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuickAction that = (QuickAction) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
