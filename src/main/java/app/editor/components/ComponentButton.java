package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import section.model.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import utils.OSValidator;

import java.util.ArrayList;
import java.util.List;

public class ComponentButton extends DragButton {
    private OnComponentActionListener onComponentActionListener;
    protected Component associatedComponent;
    private boolean isSelected = false;

    public ComponentButton(ComponentGrid componentGrid, Component associatedComponent) {
        super(componentGrid);
        this.associatedComponent = associatedComponent;

        // Add the style
        getStyleClass().add("component-btn");
        if (!associatedComponent.getItem().isValid()) {  // Invalid item rendering
            getStyleClass().add("invalid-btn");
        }

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Setup the context menu
        final ContextMenu contextMenu = new ContextMenu();
        List<MenuItem> items = new ArrayList<>();
        getContextMenu(items);
        contextMenu.getItems().addAll(items);
        setContextMenu(contextMenu);

        // Set the drag and drop
        setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (associatedComponent != null) {
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);

                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    Image snapshot = snapshot(sp, null);

                    double offsetX = 0;
                    double offsetY = 0;

                    if (OSValidator.isWindows()) {
                        offsetX = snapshot.getWidth()/2;
                        offsetY = snapshot.getHeight()/2;
                    }

                    db.setDragView(snapshot, offsetX, offsetY);

                    ClipboardContent content = new ClipboardContent();
                    content.putString(DragButton.DRAG_PREFIX+associatedComponent.json().toString());
                    db.setContent(content);

                    event.consume();
                }
            }
        });
        setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getTransferMode() == TransferMode.MOVE) {
                    if (onComponentActionListener != null) {
                        onComponentActionListener.onComponentDroppedAway();
                    }
                }

                setCursor(Cursor.DEFAULT);

                event.consume();
            }
        });
    }

    /**
     * Populate the context menu
     * @param items
     */
    protected void getContextMenu(List<MenuItem> items) {
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentEdit();
                }
            }
        });
        Image editImage = new Image(ComponentButton.class.getResourceAsStream("/assets/edit.png"));
        ImageView editImageView = new ImageView(editImage);
        editImageView.setFitWidth(16);
        editImageView.setFitHeight(16);
        edit.setGraphic(editImageView);

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentDelete();
                }
            }
        });
        Image deleteImage = new Image(ComponentButton.class.getResourceAsStream("/assets/delete.png"));
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(16);
        deleteImageView.setFitHeight(16);
        delete.setGraphic(deleteImageView);

        items.add(edit);
        items.add(delete);
    }

    protected String getParentTooltip() {
        if (associatedComponent.getItem().isValid()) {
            return "";
        }else{
            return "INVALID ITEM\n";
        }
    }

    public OnComponentActionListener getOnComponentActionListener() {
        return onComponentActionListener;
    }

    public void setOnComponentActionListener(OnComponentActionListener onComponentActionListener) {
        this.onComponentActionListener = onComponentActionListener;
    }

    public interface OnComponentActionListener {
        void onComponentEdit();
        void onComponentDelete();
        void onComponentDroppedAway();
    }
}
