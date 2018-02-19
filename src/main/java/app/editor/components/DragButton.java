package app.editor.components;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import json.JSONObject;
import section.model.Component;

public class DragButton extends Button {
    public static final String DRAG_PREFIX = "DRAG_COMPONENT";

    private OnComponentDragListener onComponentDragListener;

    private boolean isDragDestination = false;

    protected ComponentGrid componentGrid;

    public DragButton(ComponentGrid componentGrid) {
        super();
        this.componentGrid = componentGrid;

        getStyleClass().add("drag-button");

        setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != DragButton.this &&
                        event.getDragboard().hasString() && event.getDragboard().getString().startsWith(DRAG_PREFIX)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            }
        });

        setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != DragButton.this &&
                        event.getDragboard().hasString() && event.getDragboard().getString().startsWith(DRAG_PREFIX)) {
                    // Get the drop json
                    String json = event.getDragboard().getString().substring(DRAG_PREFIX.length());  // Remove the first DRAG_PREFIX string

                    // Notify the listener
                    if (onComponentDragListener != null) {
                        // Create the component
                        Component component = Component.fromJson(new JSONObject(json));
                        onComponentDragListener.onComponentDropping(component);
                    }
                }

                event.consume();
            }
        });

        setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != DragButton.this &&
                        event.getDragboard().hasString() && event.getDragboard().getString().startsWith(DRAG_PREFIX)) {
                }

                event.consume();
            }
        });

        setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                boolean success = false;

                if (event.getGestureSource() != DragButton.this &&
                        event.getDragboard().hasString() && event.getDragboard().getString().startsWith(DRAG_PREFIX)) {
                    // Get the drop json
                    String json = event.getDragboard().getString().substring(DRAG_PREFIX.length());  // Remove the first DRAG_PREFIX string

                    // Notify the listener
                    if (onComponentDragListener != null) {
                        // Create the component
                        Component component = Component.fromJson(new JSONObject(json));
                        success = onComponentDragListener.onComponentDropped(component);
                    }
                }

                event.setDropCompleted(success);

                event.consume();
            }
        });
    }

    public OnComponentDragListener getOnComponentDragListener() {
        return onComponentDragListener;
    }

    public void setOnComponentDragListener(OnComponentDragListener onComponentDragListener) {
        this.onComponentDragListener = onComponentDragListener;
    }

    public boolean isDragDestination() {
        return isDragDestination;
    }

    public void setDragDestination(boolean dragDestination, boolean overwriteDanger) {
        isDragDestination = dragDestination;
        if (isDragDestination) {
            if (!overwriteDanger) {
                getStyleClass().add("drag-entered");
            }else{
                getStyleClass().add("drag-entered-danger");
            }
        }else{
            getStyleClass().remove("drag-entered");
            getStyleClass().remove("drag-entered-danger");
        }
    }

    public interface OnComponentDragListener {
        boolean onComponentDropped(Component component);
        boolean onComponentDropping(Component component);
    }
}
