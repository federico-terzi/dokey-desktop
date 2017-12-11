package app.editor.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class ComponentButton extends Button {
    private OnComponentActionListener onComponentActionListener;

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
    }
}
