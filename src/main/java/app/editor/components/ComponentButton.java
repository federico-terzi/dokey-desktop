package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import section.model.Component;

public class ComponentButton extends DragButton {
    private OnComponentActionListener onComponentActionListener;
    protected Component associatedComponent;
    private boolean isSelected = false;

    public ComponentButton(Component associatedComponent) {
        super();
        this.associatedComponent = associatedComponent;

        getStyleClass().add("component-btn");

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
        Image editImage = new Image(ComponentButton.class.getResourceAsStream("/assets/edit.png"));
        ImageView editImageView = new ImageView(editImage);
        editImageView.setFitWidth(16);
        editImageView.setFitHeight(16);
        edit.setGraphic(editImageView);

        MenuItem expandRightItem = new MenuItem("Expand Right");
        expandRightItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentExpandRight();
                }
            }
        });
        Image rightImage = new Image(ComponentButton.class.getResourceAsStream("/assets/right_arrow.png"));
        ImageView rightImageView = new ImageView(rightImage);
        rightImageView.setFitWidth(16);
        rightImageView.setFitHeight(16);
        expandRightItem.setGraphic(rightImageView);

        MenuItem expandBottomItem = new MenuItem("Expand Down");
        expandBottomItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentExpandBottom();
                }
            }
        });
        Image bottomImage = new Image(ComponentButton.class.getResourceAsStream("/assets/down_arrow.png"));
        ImageView downImageView = new ImageView(bottomImage);
        downImageView.setFitWidth(16);
        downImageView.setFitHeight(16);
        expandBottomItem.setGraphic(downImageView);

        MenuItem shrinkLeftItem = new MenuItem("Shrink Left");
        shrinkLeftItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentShrinkLeft();
                }
            }
        });
        Image leftImage = new Image(ComponentButton.class.getResourceAsStream("/assets/left_arrow.png"));
        ImageView leftImageView = new ImageView(leftImage);
        leftImageView.setFitWidth(16);
        leftImageView.setFitHeight(16);
        shrinkLeftItem.setGraphic(leftImageView);

        MenuItem shrinkUpItem = new MenuItem("Shrink Up");
        shrinkUpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onComponentActionListener != null) {
                    onComponentActionListener.onComponentShrinkUp();
                }
            }
        });
        Image upImage = new Image(ComponentButton.class.getResourceAsStream("/assets/up_arrow.png"));
        ImageView upImageView = new ImageView(upImage);
        upImageView.setFitWidth(16);
        upImageView.setFitHeight(16);
        shrinkUpItem.setGraphic(upImageView);

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

        contextMenu.getItems().addAll(edit, new SeparatorMenuItem(), expandRightItem, shrinkLeftItem,
                expandBottomItem, shrinkUpItem, new SeparatorMenuItem(), delete);
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
