package system.bookmarks;

import system.exceptions.UnsupportedOperatingSystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is used to import user bookmarks from various browsers
 * and display them.
 */
public class BookmarkManager {
    private List<BookmarkImportAgent> importAgents = new ArrayList<>();

    private boolean bookmarksLoaded = false;

    private List<Bookmark> bookmarks = new ArrayList<>();

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public BookmarkManager() {
        // Register import agents
        try {
            importAgents.add(new ChromeBookmarkImportAgent());
        } catch (UnsupportedOperatingSystemException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the import procedure in another thread
     */
    public void startImport() {
        LOG.info("Starting bookmark import");
        new Thread(() -> {
            for (BookmarkImportAgent importAgent : importAgents) {
                boolean res = importAgent.importBookmarks();

                // Check if the importing was successful
                if (!res) {
                    LOG.info("Import failed for browser: "+importAgent.getName());
                }else{
                    List<Bookmark> currentBookmarks = importAgent.getBookmarks();

                    // Add bookmarks in the list
                    bookmarks.addAll(currentBookmarks);
                    LOG.info("Added "+currentBookmarks.size()+" bookmarks from: "+importAgent.getName());
                }
            }

            // Set bookmarks as loaded
            bookmarksLoaded = true;
        }).start();
    }

    public boolean areBookmarksLoaded() {
        return bookmarksLoaded;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public List<Bookmark> searchBookmarks(String query, int limit) {
        if (query.isEmpty() || !areBookmarksLoaded())
            return new ArrayList<>();

        // Divide the query into subqueries to
        // search each word individually
        String[] subQueries = query.split("[^\\d\\w]");

        return bookmarks.stream().filter((bookmark -> {
            boolean isContained = true;

            for (String q : subQueries) {
                if (!bookmark.title.toLowerCase().contains(q) && !bookmark.url.toLowerCase().contains(q)) {
                    isContained = false;
                    break;
                }
            }

            return isContained;
        })).limit(limit).collect(Collectors.toList());
    }
}
