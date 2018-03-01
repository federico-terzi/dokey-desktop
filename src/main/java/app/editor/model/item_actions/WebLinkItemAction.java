package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.WebLinkDialogStage;
import section.model.Component;
import section.model.WebLinkItem;

import java.io.IOException;
import java.util.ResourceBundle;

public class WebLinkItemAction extends ItemAction {
    public WebLinkItemAction(ComponentGrid componentGrid, ResourceBundle resourceBundle) {
        super(3,resourceBundle.getString("web_link"),
                resourceBundle.getString("add_web_link"), "/assets/world_black.png", componentGrid, resourceBundle);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        try {
            WebLinkDialogStage stage = new WebLinkDialogStage(componentGrid.getWebLinkResolver(), resourceBundle,
                    new WebLinkDialogStage.OnWebLinkListener() {
                @Override
                public void onWebLinkSelected(String url, String title, String imageUrl) {
                    // Create the component
                    WebLinkItem item = new WebLinkItem();
                    item.setUrl(url);
                    item.setTitle(title);
                    item.setIconID(imageUrl);

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
                    if (listener != null) {
                        listener.onActionCanceled();
                    }
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
            WebLinkDialogStage stage = new WebLinkDialogStage(componentGrid.getWebLinkResolver(),
                    resourceBundle, new WebLinkDialogStage.OnWebLinkListener() {
                @Override
                public void onWebLinkSelected(String url, String title, String imageUrl) {
                    // Create the component
                    WebLinkItem item = new WebLinkItem();
                    item.setUrl(url);
                    item.setTitle(title);
                    item.setIconID(imageUrl);

                    component.setItem(item);

                    if (listener != null) {
                        listener.onActionCompleted(component);
                    }
                }

                @Override
                public void onCanceled() {
                    if (listener != null) {
                        listener.onActionCanceled();
                    }
                }
            });
            stage.setWebLinkItem((WebLinkItem) component.getItem());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
