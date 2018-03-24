package system.search.results;

import javafx.scene.image.Image;
import system.DebugManager;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class DebugResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "debug_category";

    private final DebugManager debugManager;
    private String command;

    public DebugResult(SearchEngine searchEngine, ResourceBundle resourceBundle, DebugManager debugManager, String query,
                       Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.debugManager = debugManager;
        this.command = query.substring(2).trim();
    }

    @Override
    public String getTitle() {
        return "<"+command+">";
    }

    @Override
    public String getDescription() {
        return "Execute \""+ command +"\" in the Developer Mode";
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
        debugManager.invokeCommand(command);
    }
}
