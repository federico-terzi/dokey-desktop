package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import system.model.Application;

import java.io.File;

public class EmptyButton extends DragButton {
    private OnEmptyBtnActionListener onEmptyBtnActionListener;

    public EmptyButton() {
        super();

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getStyleClass().add("empty-btn");

        Image image = new Image(EmptyButton.class.getResourceAsStream("/assets/add.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        imageView.setSmooth(true);
        setGraphic(imageView);
        setContentDisplay(ContentDisplay.TOP);

        // Set up the context menu
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem appLauncherItem = new MenuItem("Add Application");
        appLauncherItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onEmptyBtnActionListener != null) {
                    onEmptyBtnActionListener.onAddApplication();
                }
            }
        });
        Image appLauncherImage = new Image(ComponentButton.class.getResourceAsStream("/assets/launcher.png"));
        ImageView appLauncherImageView = new ImageView(appLauncherImage);
        appLauncherImageView.setFitWidth(32);
        appLauncherImageView.setFitHeight(32);
        appLauncherItem.setGraphic(appLauncherImageView);

        MenuItem shortcutItem = new MenuItem("Add Shortcut");
        shortcutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onEmptyBtnActionListener != null) {
                    onEmptyBtnActionListener.onAddShortcut();
                }
            }
        });
        Image shortcutImage = new Image(ComponentButton.class.getResourceAsStream("/assets/keyboard.png"));
        ImageView shortcutTmageView = new ImageView(shortcutImage);
        shortcutTmageView.setFitWidth(32);
        shortcutTmageView.setFitHeight(32);
        shortcutItem.setGraphic(shortcutTmageView);

        MenuItem folderItem = new MenuItem("Add Folder");
        folderItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onEmptyBtnActionListener != null) {
                    onEmptyBtnActionListener.onAddFolder();
                }
            }
        });
        Image folderImage = new Image(ComponentButton.class.getResourceAsStream("/assets/folder.png"));
        ImageView folderImageView = new ImageView(folderImage);
        folderImageView.setFitWidth(32);
        folderImageView.setFitHeight(32);
        folderItem.setGraphic(folderImageView);

        MenuItem internetItem = new MenuItem("Add Web Link");
        internetItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onEmptyBtnActionListener != null) {
                    onEmptyBtnActionListener.onAddWebLink();
                }
            }
        });
        Image internetImage = new Image(ComponentButton.class.getResourceAsStream("/assets/world.png"));
        ImageView internetImageView = new ImageView(internetImage);
        internetImageView.setFitWidth(32);
        internetImageView.setFitHeight(32);
        internetItem.setGraphic(internetImageView);

        MenuItem systemItem = new MenuItem("Add System Control");
        systemItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onEmptyBtnActionListener != null) {
                    onEmptyBtnActionListener.onAddSystem();
                }
            }
        });
        Image systemImage = new Image(ComponentButton.class.getResourceAsStream("/assets/shutdown.png"));
        ImageView systemImageView = new ImageView(systemImage);
        systemImageView.setFitWidth(32);
        systemImageView.setFitHeight(32);
        systemItem.setGraphic(systemImageView);

        contextMenu.getItems().addAll(appLauncherItem, shortcutItem, folderItem, internetItem, systemItem);
        setContextMenu(contextMenu);

        // Open the context menu with the right click
        addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                contextMenu.show(EmptyButton.this, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public void setOnEmptyBtnActionListener(OnEmptyBtnActionListener onEmptyBtnActionListener) {
        this.onEmptyBtnActionListener = onEmptyBtnActionListener;
    }

    public interface OnEmptyBtnActionListener {
        void onAddApplication();
        void onAddShortcut();
        void onAddFolder();
        void onAddWebLink();
        void onAddSystem();
    }
}
