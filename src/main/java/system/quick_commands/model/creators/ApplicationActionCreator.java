package system.quick_commands.model.creators;

import app.editor.stages.AppSelectDialogStage;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.QuickAction;

import java.io.IOException;
import java.util.ResourceBundle;

public class ApplicationActionCreator extends QuickActionCreator {
    public ApplicationActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(resolver, resourceBundle);
    }

    @Override
    public void createAction(OnQuickActionListener listener) {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(resolver.getApplicationManager(),
                    resourceBundle,
                    new AppSelectDialogStage.OnApplicationListener() {
                        @Override
                        public void onApplicationSelected(Application application) {
                            // Create the action
                            ApplicationAction action = new ApplicationAction();
                            action.setExecutablePath(application.getExecutablePath());

                            if (listener != null) {
                                listener.onQuickActionSelected(action);
                            }
                        }

                        @Override
                        public void onCanceled() {
                            if (listener != null) {
                                listener.onCanceled();
                            }
                        }
                    });
            appSelectDialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editAction(QuickAction action, OnQuickActionListener listener) {

    }

    @Override
    public String getDisplayText() {
        return "Open Application";  // TODO: i18n
    }
}
