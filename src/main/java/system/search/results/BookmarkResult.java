package system.search.results;

import javafx.scene.image.Image;
import system.bookmarks.Bookmark;
import system.model.ApplicationManager;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class BookmarkResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "bookmark_category";

    private Bookmark bookmark;
    private ApplicationManager applicationManager;

    public BookmarkResult(SearchEngine searchEngine, ResourceBundle resourceBundle, Bookmark bookmark,
                          ApplicationManager applicationManager, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
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
