package app.editor.stages;

import app.editor.controllers.ShortcutDialogController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import net.model.KeyboardKeys;

import java.io.IOException;
import java.util.*;

public class ShortcutDialogStage extends Stage {
    private ShortcutDialogController controller;
    private OnShortcutListener onShortcutListener;

    private List<KeyboardKeys> keys = new ArrayList<>();

    public ShortcutDialogStage(OnShortcutListener onShortcutListener) throws IOException {
        this.onShortcutListener = onShortcutListener;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/shortcut_dialog.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle("Shortcut");
        this.setScene(scene);
        this.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (ShortcutDialogController) fxmlLoader.getController();

        // Setup the button image
        Image image = new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/clear.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        imageView.setSmooth(true);
        controller.getClearShortcutBtn().setGraphic(imageView);

        // Set the event listeners
        controller.getCancelBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onShortcutListener.onCanceled();
                close();
            }
        });

        controller.getSelectBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (keys.size() > 0) {
                    String name = controller.getNameTextField().getText();
                    if (name.isEmpty()) {
                        name = getShortcut();
                    }
                    onShortcutListener.onShortcutSelected(getShortcut(),name);
                    close();
                }else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Shortcut Not Specified");
                    alert.setHeaderText("You haven't selected any shortcut yet!");
                    alert.setContentText("Please go back and type the keyboard keys or Cancel...");

                    alert.showAndWait();
                }
            }
        });

        controller.getClearShortcutBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                keys = new ArrayList<>();
                renderKeys();

                // Focus the text field
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.getShortcutTextField().requestFocus();
                    }
                });
            }
        });

        // Setup the keyboard keys onShortcutListener
        controller.getShortcutTextField().addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            KeyboardKeys keyboardKey = KeyboardKeys.findFromName(key.getCode().getName().toUpperCase());
            if (keyboardKey != null && !keys.contains(keyboardKey)) {
                keys.add(keyboardKey);
                renderKeys();
            }
        });

        // Focus the text field
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.getShortcutTextField().requestFocus();
            }
        });
    }

    private void renderKeys() {
        controller.getShortcutTextField().setText(getShortcut());
    }

    private String getShortcut() {
        StringJoiner joiner = new StringJoiner("+");
        for (KeyboardKeys key : keys) {
            joiner.add(key.getKeyName());
        }
        return joiner.toString();
    }

    public interface OnShortcutListener {
        void onShortcutSelected(String shortcut, String name);
        void onCanceled();
    }
}
