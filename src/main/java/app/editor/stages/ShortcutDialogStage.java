package app.editor.stages;

import app.editor.controllers.ShortcutDialogController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import net.model.IconTheme;
import net.model.KeyboardKeys;
import section.model.ShortcutItem;
import system.ResourceUtils;
import system.ShortcutIcon;
import system.ShortcutIconManager;
import utils.OSValidator;

import java.io.IOException;
import java.util.*;

public class ShortcutDialogStage extends Stage {
    private ShortcutIconManager shortcutIconManager;
    private ShortcutDialogController controller;
    private ResourceBundle resourceBundle;
    private OnShortcutListener onShortcutListener;

    private List<KeyboardKeys> keys = new ArrayList<>();
    private ShortcutIcon icon = null;

    public ShortcutDialogStage(ShortcutIconManager shortcutIconManager, ResourceBundle resourceBundle, OnShortcutListener onShortcutListener) throws IOException {
        this.shortcutIconManager = shortcutIconManager;
        this.resourceBundle = resourceBundle;
        this.onShortcutListener = onShortcutListener;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/shortcut_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle(resourceBundle.getString("shortcut"));
        this.setScene(scene);
        this.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
        scene.getStylesheets().add(ResourceUtils.getResource("/css/main.css").toURI().toString());
        setResizable(false);
        setAlwaysOnTop(true);

        controller = (ShortcutDialogController) fxmlLoader.getController();

        // Setup the button image
        Image image = new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/clear.png"), 32, 32, true, true);
        ImageView imageView = new ImageView(image);
        controller.getClearShortcutBtn().setGraphic(imageView);

        // Setup the icon button
        Image iconImage = new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/cake.png"), 32, 32, true, true);
        ImageView iconImageView = new ImageView(iconImage);
        controller.iconBtn.setGraphic(iconImageView);
        controller.iconBtn.setContentDisplay(ContentDisplay.TOP);

        // Setup the meta button based on the os
        controller.metaBtn.setText(OSValidator.isWindows() ? "WIN" : "CMD");

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
                    onShortcutListener.onShortcutSelected(getShortcut(),name, icon);
                    close();
                }else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(resourceBundle.getString("shortcut_not_specified"));
                    alert.setHeaderText(resourceBundle.getString("shortcut_not_specified_msg"));
                    alert.setContentText(resourceBundle.getString("shortcut_not_specified_msg2"));

                    alert.showAndWait();
                }
            }
        });

        controller.iconBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ShortcutIconDialogStage shortcutIconDialogStage = null;
                try {
                    shortcutIconDialogStage = new ShortcutIconDialogStage(shortcutIconManager, resourceBundle, new ShortcutIconDialogStage.OnIconSelectListener() {
                        @Override
                        public void onIconSelected(ShortcutIcon icon) {
                            ShortcutDialogStage.this.icon = icon;
                            renderIcon();
                        }

                        @Override
                        public void onCanceled() {

                        }
                    });
                    shortcutIconDialogStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
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
            KeyboardKeys keyboardKey = KeyboardKeys.findFromConvertedName(key.getCode().getName().toUpperCase());
            if (keyboardKey != null && !keys.contains(keyboardKey)) {
                keys.add(keyboardKey);
                renderKeys();
            }
        });

        // Add special keys handlers
        for (int i = 0; i<controller.specialKeys.length; i++) {
            Button btn = controller.getSpecialBtns()[i];
            KeyboardKeys key = controller.specialKeys[i];

            // Convert the meta key to the corresponding key based on the OS.
            if (key == KeyboardKeys.VK_META) {
                key = (OSValidator.isWindows() ? KeyboardKeys.VK_WINDOWS : KeyboardKeys.VK_COMMAND);
            }

            final KeyboardKeys finalKey = key;

            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (!keys.contains(finalKey)) {
                        keys.add(finalKey);
                        renderKeys();
                    }
                }
            });
        }

        // Focus the text field
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.getShortcutTextField().requestFocus();
            }
        });
    }

    private void renderKeys() {
        StringJoiner joiner = new StringJoiner("+");
        for (KeyboardKeys key : keys) {
            String keyName = key.getKeyName(OSValidator.getOS());

            joiner.add(keyName);
        }
        controller.getShortcutTextField().setText(joiner.toString());
    }

    private void renderIcon() {
        if (icon != null) {
            Image image = new Image(icon.getFile().toURI().toString(), 32, 32, true, true);
            ImageView imageView = new ImageView(image);
            controller.iconBtn.setGraphic(imageView);
            controller.iconBtn.setText(resourceBundle.getString("change_icon_btn"));
        }else{
            controller.iconBtn.setGraphic(null);
            controller.iconBtn.setText(resourceBundle.getString("select_icon_btn"));
        }

    }

    private String getShortcut() {
        StringJoiner joiner = new StringJoiner("+");
        for (KeyboardKeys key : keys) {
            joiner.add(key.getKeyName());
        }
        return joiner.toString();
    }

    public void setShortcutItem(ShortcutItem item) {
        controller.getNameTextField().setText(item.getTitle());
        // Add the shortcuts
        StringTokenizer st = new StringTokenizer(item.getShortcut(), "+");
        while(st.hasMoreTokens()) {
            KeyboardKeys keyboardKey = KeyboardKeys.findFromName(st.nextToken().trim());
            if (keyboardKey != null && !keys.contains(keyboardKey)) {
                keys.add(keyboardKey);
                renderKeys();
            }
        }

        if (item.getIconID() != null) {
            ShortcutIcon icon = shortcutIconManager.getIcon(item.getIconID(), IconTheme.LIGHT);
            if (icon != null) {
                this.icon = icon;
                renderIcon();
            }
        }
    }

    public interface OnShortcutListener {
        void onShortcutSelected(String shortcut, String name, ShortcutIcon icon);
        void onCanceled();
    }
}
