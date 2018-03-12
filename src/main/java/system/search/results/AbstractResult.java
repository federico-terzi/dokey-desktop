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
        void onImageAvailable(Image image, String hashID);
    }

    /**
     * Get the hash for the result or null if is is not cache-able.
     * @return the Hash identifier of the current result if cache-able, null otherwise.
     */
    public String getHash() {
        return null;
    }

    /**
     * Execute the action associated with this result
     */
    public abstract void executeAction();
}
