package system.search.results;

import javafx.scene.image.Image;
import system.DebugManager;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class DebugResult extends AbstractResult {
    private final DebugManager debugManager;
    private String command;

    public DebugResult(SearchEngine searchEngine, ResourceBundle resourceBundle, DebugManager debugManager, String query) {
        super(searchEngine, resourceBundle);
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
                Image appImage = new Image(DebugResult.class.getResourceAsStream("/assets/bug.png"), 32, 32, true, true);
                listener.onImageAvailable(appImage, null);
            }).start();
        }
    }

    @Override
    public void executeAction() {
        debugManager.invokeCommand(command);
    }
}
