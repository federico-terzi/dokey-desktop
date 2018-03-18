package system.quick_commands.model.creators;

import app.editor.stages.AppSelectDialogStage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.QuickAction;

import java.io.IOException;
import java.util.ResourceBundle;

public class ApplicationActionCreator extends QuickActionCreator {
    private Button selectAppButton;
    private Label applicationLabel;

    public ApplicationActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(QuickAction.Type.APP, resolver, resourceBundle);
    }

    @Override
    public void createActionBox(VBox box, OnActionModifiedListener listener) {
        applicationLabel = new Label("No application currently selected");  // TODO: I18n
        applicationLabel.setPadding(new Insets(5, 0, 5, 0));
        box.getChildren().add(applicationLabel);

        selectAppButton = new Button("Select Application...");
        selectAppButton.setOnAction(event -> {
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
                                    listener.onActionModified(action);
                                }
                            }

                            @Override
                            public void onCanceled() {

                            }
                        });
                appSelectDialogStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        box.getChildren().add(selectAppButton);
    }

    @Override
    public void renderActionBox(QuickAction action) {
        if (action == null) {
            applicationLabel.setText("No application currently selected");  // TODO: i18n
        }else{
            applicationLabel.setText(action.getDisplayText(resolver, resourceBundle));
        }
    }

    @Override
    public String getDisplayText() {
        return "Open Application";  // TODO: i18n
    }
}
