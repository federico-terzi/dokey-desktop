package system.search.results;

import javafx.scene.image.Image;

/**
 * This class represents a generic search result
 */
public abstract class AbstractResult {
    public abstract String getTitle();
    public abstract String getDescription();
    public abstract void requestImage(OnImageAvailableListener listener);

    public interface OnImageAvailableListener {
        void onImageAvailable(Image image);
    }
}
