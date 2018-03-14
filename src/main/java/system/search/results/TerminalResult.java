package system.search.results;

import javafx.scene.image.Image;
import system.search.SearchEngine;
import utils.ImageResolver;

import java.util.ResourceBundle;

public class TerminalResult extends AbstractResult {
    private String command;

    public TerminalResult(SearchEngine searchEngine, ResourceBundle resourceBundle, String query, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.command = query.substring(1).trim();
    }

    @Override
    public String getTitle() {
        return command;
    }

    @Override
    public String getDescription() {
        return "Execute \""+ command +"\" in the Terminal";  // TODO: i18n
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
        searchEngine.getApplicationManager().openTerminalWithCommand(command);
    }
}
