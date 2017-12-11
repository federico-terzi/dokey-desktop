package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import section.model.Component;
import section.model.Item;

public class ComponentButton extends DragButton {
    private OnComponentActionListener onComponentActionListener;
    private Component associatedComponent;


    public ComponentButton() {
        super();

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Setup the context menu
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentDelete();
                }
            }
        });
        contextMenu.getItems().addAll(edit, delete);
        setContextMenu(contextMenu);

        // Set the drag and drop
        setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (associatedComponent != null) {
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);

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
                event.consume();
            }
        });
    }

    public OnComponentActionListener getOnComponentActionListener() {
        return onComponentActionListener;
    }

    public void setOnComponentActionListener(OnComponentActionListener onComponentActionListener) {
        this.onComponentActionListener = onComponentActionListener;
    }

    public void setAssociatedComponent(Component associatedComponent) {
        this.associatedComponent = associatedComponent;
    }

    public interface OnComponentActionListener {
        void onComponentEdit();
        void onComponentDelete();
        void onComponentDroppedAway();
    }
}
