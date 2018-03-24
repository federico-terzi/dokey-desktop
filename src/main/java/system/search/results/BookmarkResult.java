package system.search.results;

import javafx.scene.image.Image;
import system.bookmarks.Bookmark;
import system.model.ApplicationManager;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class BookmarkResult extends AbstractResult {
    private Bookmark bookmark;
    private ApplicationManager applicationManager;

    public BookmarkResult(SearchEngine searchEngine, ResourceBundle resourceBundle, Bookmark bookmark,
                          ApplicationManager applicationManager, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage, 200);
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
                    listener.onImageAvailable(defaultImage, null);
                }).start();
            }).start();
        }
    }

    @Override
    public void executeAction() {
        applicationManager.openWebLink(bookmark.url);
    }
}
