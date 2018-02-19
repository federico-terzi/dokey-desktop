package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.SystemDialogStage;
import app.editor.stages.WebLinkDialogStage;
import section.model.Component;
import section.model.SystemItem;
import section.model.WebLinkItem;

import java.io.IOException;

public class SystemItemAction extends ItemAction {
    public SystemItemAction(ComponentGrid componentGrid) {
        super("System", "Add System Control", "/assets/shutdown.png", componentGrid);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        try {
            SystemDialogStage stage = new SystemDialogStage(new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
                    // Create the component
                    Component component = new Component();
                    component.setItem(item);
                    component.setX(row);
                    component.setY(col);

                    if (listener != null) {
                        listener.onActionCompleted(component);
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestEditItem(Component component, OnActionCompletedListener listener) {
        try {
            SystemDialogStage stage = new SystemDialogStage(new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
                    component.setItem(item);

                    if (listener != null) {
                        listener.onActionCompleted(component);
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            stage.setSystemItem((SystemItem) component.getItem());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
