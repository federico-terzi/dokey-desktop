package system.quick_commands.model.creators;

import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.QuickAction;

import java.util.ResourceBundle;

/**
 * The creator for a specific type of QuickAction.
 * It makes available methods to create and exit a quick action
 */
public abstract class QuickActionCreator {
    protected DependencyResolver resolver;
    protected ResourceBundle resourceBundle;  //  the ResourceBundle used for the i18n.

    public QuickActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        this.resolver = resolver;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Display a dialog to create the action.
     * @param listener that will handle the returned action.
     */
    public abstract void createAction(OnQuickActionListener listener);

    /**
     * Display a dialog to edit the given action.
     * @param action the action to edit.
     * @param listener that will handle the returned action.
     */
    public abstract void editAction(QuickAction action, OnQuickActionListener listener);

    /**
     * @return the human readable creator description.
     */
    public abstract String getDisplayText();

    public interface OnQuickActionListener {
        void onQuickActionSelected(QuickAction action);
        void onCanceled();
    }
}
