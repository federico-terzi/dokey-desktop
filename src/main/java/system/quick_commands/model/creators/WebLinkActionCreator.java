package system.quick_commands.model.creators;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.QuickAction;
import system.quick_commands.model.actions.WebLinkAction;

import java.net.URL;
import java.util.ResourceBundle;

public class WebLinkActionCreator extends QuickActionCreator {
    private TextField urlTextField;

    public WebLinkActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(QuickAction.Type.WEB_LINK, resolver, resourceBundle);
    }

    @Override
    public void createActionBox(VBox box, OnActionModifiedListener listener) {
        urlTextField = new TextField();
        urlTextField.setPromptText(resourceBundle.getString("insert_url_here"));
        urlTextField.setMaxWidth(Double.MAX_VALUE);

        urlTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            String url = validateURL(newValue);

            // Validate URL
            if (url != null) {
                WebLinkAction action = new WebLinkAction();
                action.setUrl(url);
                if (listener != null) {
                    listener.onActionModified(action);
                }
            }else{  // INVALID URL
                if (listener != null) {
                    listener.onActionModified(null);
                }
            }
        }));

        box.getChildren().add(urlTextField);
    }

    @Override
    public void renderActionBox(QuickAction action) {
        if (action != null)
            urlTextField.setText(((WebLinkAction) action).getUrl());
    }

    private static String validateURL(String urlQuery) {
        // Make sure the url is valid
        try {
            // Consider the case the url doesn't have the scheme ( HTTP )
            // a basic check is provided ( contains a dot )
            if (urlQuery.contains(".") && !urlQuery.startsWith("http")) {
                urlQuery = "http://"+urlQuery;
            }

            final String url = urlQuery;

            URL u = new URL(url);
            u.toURI();

            return url;
        } catch (Exception e) {}
        return null;
    }

    @Override
    public String getDisplayText() {
        return resourceBundle.getString("navigate_to_url");
    }
}
