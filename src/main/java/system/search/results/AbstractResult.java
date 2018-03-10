package system.search.results;

import javafx.scene.image.Image;
import system.search.SearchEngine;

import java.util.ResourceBundle;

/**
 * This class represents a generic search result
 */
public abstract class AbstractResult {
    protected SearchEngine searchEngine;
    protected ResourceBundle resourceBundle;

    protected AbstractResult(SearchEngine searchEngine, ResourceBundle resourceBundle) {
        this.searchEngine = searchEngine;
        this.resourceBundle = resourceBundle;
    }

    public abstract String getTitle();
    public abstract String getDescription();
    public abstract void requestImage(OnImageAvailableListener listener);

    public interface OnImageAvailableListener {
        void onImageAvailable(Image image);
    }

    /**
     * Execute the action associated with this result
     */
    public abstract void executeAction();
}
