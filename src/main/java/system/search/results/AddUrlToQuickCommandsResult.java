package system.search.results;

import javafx.scene.image.Image;
import system.BroadcastManager;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class AddUrlToQuickCommandsResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "add_url_to_quick_commands_category";

    private String url;

    public AddUrlToQuickCommandsResult(SearchEngine searchEngine, ResourceBundle resourceBundle, String query, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.url = query.trim();
    }

    @Override
    public String getTitle() {
        return "Add URL to Quick Commands"; // TODO i18n
    }

    @Override
    public String getDescription() {
        return url;
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                listener.onImageAvailable(defaultImage, null);
            }).start();
        }
    }

    @Override
    public void executeAction() {
        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_COMMANDS_REQUEST_EVENT, null);
        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.ADD_URL_TO_QUICK_COMMANDS_EVENT, this.url);
    }
}
