package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import section.model.Component;

public class ComponentButton extends DragButton {
    private OnComponentActionListener onComponentActionListener;
    protected Component associatedComponent;
    private boolean isSelected = false;

    public ComponentButton(Component associatedComponent) {
        super();
        this.associatedComponent = associatedComponent;

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Setup the context menu
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showEditDialog();
            }
        });
        MenuItem expandRightItem = new MenuItem("Expand to Right");
        expandRightItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentExpandRight();
                }
            }
        });
        MenuItem expandBottomItem = new MenuItem("Expand to Bottom");
        expandBottomItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentExpandBottom();
                }
            }
        });
        MenuItem shrinkLeftItem = new MenuItem("Shrink to Left");
        shrinkLeftItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentShrinkLeft();
                }
            }
        });
        MenuItem shrinkUpItem = new MenuItem("Shrink to Up");
        shrinkUpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentShrinkUp();
                }
            }
        });

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentDelete();
                }
            }
        });
        contextMenu.getItems().addAll(edit, new SeparatorMenuItem(), expandRightItem, expandBottomItem,
                shrinkLeftItem, shrinkUpItem, new SeparatorMenuItem(), delete);
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        if (isSelected) {
            getStyleClass().add("selected");
        }else{
            getStyleClass().remove("selected");
        }
    }

    public void showEditDialog() {
        // Implement in subclasses
    }

    public interface OnComponentActionListener {
        void onComponentEdit();
        void onComponentDelete();
        void onComponentDroppedAway();
        void onComponentExpandRight();
        void onComponentExpandBottom();
        void onComponentShrinkLeft();
        void onComponentShrinkUp();
    }
}
