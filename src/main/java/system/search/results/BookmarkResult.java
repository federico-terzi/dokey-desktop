package system.search.results;

import javafx.scene.image.Image;
import system.bookmarks.Bookmark;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import utils.ImageResolver;

import java.util.ResourceBundle;

public class BookmarkResult extends AbstractResult {
    private Bookmark bookmark;
    private ApplicationManager applicationManager;

    public BookmarkResult(SearchEngine searchEngine, ResourceBundle resourceBundle, Bookmark bookmark,
                          ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle);
        this.bookmark = bookmark;
        this.applicationManager = applicationManager;
    }

    @Override
    public String getTitle() {
        return bookmark.title;
    }

    @Override
    public String getDescription() {
        return bookmark.url;
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                new Thread(() -> {
                    Image image = ImageResolver.getInstance().getImage(BookmarkResult.class.getResourceAsStream("/assets/star.png"), 32);
                    listener.onImageAvailable(image, null);
                }).start();
            }).start();
        }
    }

    @Override
    public void executeAction() {
        applicationManager.openWebLink(bookmark.url);
    }
}
