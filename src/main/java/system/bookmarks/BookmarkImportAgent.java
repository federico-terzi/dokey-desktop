package system.bookmarks;

import java.util.List;
import java.util.logging.Logger;

/**
 * Tha abstract importer, that will import all user bookmarks.
 */
public interface BookmarkImportAgent {
    String getName();
    boolean importBookmarks();
    List<Bookmark> getBookmarks();

    Logger LOG = Logger.getGlobal();

    class BookmarksNotFoundException extends Exception {
        public BookmarksNotFoundException(String message) {
            super(message);
        }
    }
}
