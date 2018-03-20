package system.quick_commands.model.creators;

import app.editor.stages.AppSelectDialogStage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.FolderAction;
import system.quick_commands.model.actions.QuickAction;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class FolderActionCreator extends QuickActionCreator {
    private Button selectFolderApp;
    private Label folderLabel;

    public FolderActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(QuickAction.Type.FOLDER, resolver, resourceBundle);
    }

    @Override
    public void createActionBox(VBox box, OnActionModifiedListener listener) {
        folderLabel = new Label(resourceBundle.getString("no_folder_selected"));
        folderLabel.setPadding(new Insets(5, 0, 5, 0));
        box.getChildren().add(folderLabel);

        selectFolderApp = new Button(resourceBundle.getString("select_folder"));
        selectFolderApp.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(resourceBundle.getString("choose_the_folder"));
            File selectedDirectory = chooser.showDialog(null);

            if (selectedDirectory != null) {
                FolderAction action = new FolderAction();
                action.setPath(selectedDirectory.getAbsolutePath());

                if (listener != null) {
                    listener.onActionModified(action);
                }
            }
        });
        box.getChildren().add(selectFolderApp);
    }

    @Override
    public void renderActionBox(QuickAction action) {
        if (action == null) {
            folderLabel.setText(resourceBundle.getString("no_folder_selected"));
        }else{
            folderLabel.setText(action.getDisplayText(resolver, resourceBundle));
        }
    }

    @Override
    public String getDisplayText() {
        return resourceBundle.getString("open_folder");
    }
}
