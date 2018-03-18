package system.quick_commands.model.creators;

import javafx.scene.layout.VBox;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.QuickAction;

import java.util.ResourceBundle;

/**
 * The creator for a specific type of QuickAction.
 * It makes available methods to create and exit a quick action
 */
public abstract class QuickActionCreator {
    protected QuickAction.Type actionType;
    protected DependencyResolver resolver;
    protected ResourceBundle resourceBundle;  //  the ResourceBundle used for the i18n.

    protected QuickActionCreator(QuickAction.Type actionType, DependencyResolver resolver, ResourceBundle resourceBundle) {
        this.actionType = actionType;
        this.resolver = resolver;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Used to inject in the given VBox all the widgets needed to customize the action editor.
     * @param box the VBox container.
     * @param listener that will handle all the modifications of the action.
     */
    public abstract void createActionBox(VBox box, OnActionModifiedListener listener);

    /**
     * Update the widgets to reflect the status of the given action.
     * @param action the action to reflect
     */
    public abstract void renderActionBox(QuickAction action);

    public interface OnActionModifiedListener {
        void onActionModified(QuickAction action);
    }

    /**
     * @return the human readable creator description.
     */
    public abstract String getDisplayText();

    public QuickAction.Type getActionType() {
        return actionType;
    }

    public void setActionType(QuickAction.Type actionType) {
        this.actionType = actionType;
    }
}
