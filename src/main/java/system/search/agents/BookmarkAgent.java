package system.search.agents;

import system.bookmarks.BookmarkManager;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.ApplicationResult;
import system.search.results.BookmarkResult;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BookmarkAgent extends AbstractAgent {
    private BookmarkManager bookmarkManager;
    private ApplicationManager applicationManager;

    public BookmarkAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, BookmarkManager bookmarkManager,
                         ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle);

        this.bookmarkManager = bookmarkManager;
        this.applicationManager = applicationManager;
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        String lowerCaseQuery = query.toLowerCase();

        List<BookmarkResult> results = bookmarkManager.searchBookmarks(query.toLowerCase()).stream().map((bookmark -> {
            return new BookmarkResult(searchEngine, resourceBundle, bookmark, applicationManager);
        })).collect(Collectors.toList());
        return results;
    }
}
