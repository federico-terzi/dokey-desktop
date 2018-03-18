package system.quick_commands.model.creators;

import app.editor.stages.AppSelectDialogStage;
import app.editor.stages.WebLinkDialogStage;
import system.model.Application;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.QuickAction;
import system.quick_commands.model.actions.WebLinkAction;

import java.io.IOException;
import java.util.ResourceBundle;

public class WebLinkActionCreator extends QuickActionCreator {
    public WebLinkActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(QuickAction.Type.WEB_LINK, resolver, resourceBundle);
    }

    @Override
    public void createAction(OnQuickActionListener listener) {
        try {
            WebLinkDialogStage stage = new WebLinkDialogStage(resolver.getWebLinkResolver(), resourceBundle,
                    new WebLinkDialogStage.OnWebLinkListener() {
                        @Override
                        public void onWebLinkSelected(String url, String title, String imageUrl) {
                            WebLinkAction action = new WebLinkAction();
                            action.setUrl(url);

                            if (listener != null) {
                                listener.onQuickActionSelected(action);
                            }
                        }

                        @Override
                        public void onCanceled() {
                            if (listener != null) {
                                listener.onCanceled();
                            }
                        }
                    });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editAction(QuickAction action, OnQuickActionListener listener) {

    }

    @Override
    public String getDisplayText() {
        return "Navigate to URLgm";  // TODO: i18n
    }
}
