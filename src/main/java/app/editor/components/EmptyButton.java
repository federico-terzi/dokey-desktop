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

    public EmptyButton(ComponentGrid componentGrid) {
        super(componentGrid);

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getStyleClass().add("empty-btn");

        Image image = new Image(EmptyButton.class.getResourceAsStream("/assets/add_clean.png"), 24, 24, true, true);
        ImageView imageView = new ImageView(image);
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
        Image appLauncherImage = new Image(ComponentButton.class.getResourceAsStream("/assets/launcher.png"), 32, 32, true, true);
        ImageView appLauncherImageView = new ImageView(appLauncherImage);
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
        Image shortcutImage = new Image(ComponentButton.class.getResourceAsStream("/assets/keyboard.png"), 32, 32, true, true);
        ImageView shortcutTmageView = new ImageView(shortcutImage);
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
        Image folderImage = new Image(ComponentButton.class.getResourceAsStream("/assets/folder.png"), 32, 32, true, true);
        ImageView folderImageView = new ImageView(folderImage);
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
        Image internetImage = new Image(ComponentButton.class.getResourceAsStream("/assets/world.png"), 32, 32, true, true);
        ImageView internetImageView = new ImageView(internetImage);
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
        Image systemImage = new Image(ComponentButton.class.getResourceAsStream("/assets/shutdown.png"), 32, 32, true, true);
        ImageView systemImageView = new ImageView(systemImage);
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
