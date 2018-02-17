package app.editor.stages;

import app.editor.controllers.SystemDialogController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import section.model.SystemCommands;
import section.model.SystemItem;
import system.ResourceUtils;
import utils.SystemItemManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

public class SystemDialogStage extends Stage {
    private final SystemDialogController controller;
    private OnSystemItemListener listener;

    public SystemDialogStage(OnSystemItemListener listener) throws IOException {
        this.listener = listener;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/system_dialog.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        //scene.getStylesheets().add(ResourceUtils.getResource("/css/applistcell.css").toURI().toString());
        this.setTitle("System Controls");
        this.setScene(scene);
        this.getIcons().add(new Image(SystemDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SystemDialogController) fxmlLoader.getController();

        populateSystemListView();

        // Set the event listeners
        controller.cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listener.onCanceled();
                close();
            }
        });

        controller.selectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SystemCommands commandType = (SystemCommands) controller.systemListView.getSelectionModel().getSelectedItem();

                if (commandType != null) {
                    SystemItem item = new SystemItem();
                    item.setRequiresConfirmation(controller.confirmCheckBox.isSelected());
                    item.setCommandType(commandType);
                    item.setTitle(commandType.getTitle());

                    listener.onSystemItemSelected(item);
                    close();
                }
            }
        });

        controller.systemListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SystemCommands>() {
            @Override
            public void changed(ObservableValue<? extends SystemCommands> observable, SystemCommands oldValue, SystemCommands newValue) {
                controller.confirmCheckBox.setSelected(newValue.shouldAskForConfirmation());
            }
        });
    }

    private void populateSystemListView() {
        ObservableList<SystemCommands> commands = FXCollections.observableArrayList(SystemItemManager.getCommands());

        Collections.sort(commands, new Comparator<SystemCommands>() {
            @Override
            public int compare(SystemCommands o1, SystemCommands o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        controller.systemListView.setItems(commands);
    }

    public void setSystemItem(SystemItem item) {
        controller.systemListView.getSelectionModel().select(item.getCommandType());
        controller.confirmCheckBox.setSelected(item.requiresConfirmation());
    }

    public interface OnSystemItemListener {
        void onSystemItemSelected(SystemItem item);
        void onCanceled();
    }
}
