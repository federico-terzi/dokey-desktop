package app.editor.components;

import app.editor.model.item_actions.ItemAction;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import section.model.Component;
import section.model.ItemType;
import system.model.Application;

import java.io.File;
import java.util.List;
import java.util.Map;

public class EmptyButton extends DragButton {
    private OnEmptyBtnActionListener listener;

    public EmptyButton(ComponentGrid componentGrid, List<ItemAction> itemActions,
                       OnEmptyBtnActionListener listener) {
        super(componentGrid);
        this.listener = listener;

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Set up the button design
        getStyleClass().add("empty-btn");
        Image image = new Image(EmptyButton.class.getResourceAsStream("/assets/add_clean.png"), 24, 24, true, true);
        ImageView imageView = new ImageView(image);
        setGraphic(imageView);
        setContentDisplay(ContentDisplay.TOP);

        // Set up the context menu
        final ContextMenu contextMenu = new ContextMenu();
        for (ItemAction itemAction : itemActions) {
            MenuItem item = itemAction.getContextMenuItem();
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (listener != null) {
                        listener.onActionSelected(itemAction);
                    }
                }
            });
            contextMenu.getItems().add(item);
        }
        setContextMenu(contextMenu);

        // Open the context menu with the right click
        addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                contextMenu.show(EmptyButton.this, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public interface OnEmptyBtnActionListener {
        void onActionSelected(ItemAction action);
    }
}
