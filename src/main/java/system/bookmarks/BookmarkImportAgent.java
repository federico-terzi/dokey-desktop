package system.bookmarks;

import java.util.List;

/**
 * Tha abstract importer, that will import all user bookmarks.
 */
public interface BookmarkImportAgent {
    String getName();
    boolean importBookmarks();
    List<Bookmark> getBookmarks();

    class BookmarksNotFoundException extends Exception {
        public BookmarksNotFoundException(String message) {
            super(message);
        }
    }
}
