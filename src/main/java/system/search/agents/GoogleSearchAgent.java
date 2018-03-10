package system.search.agents;

import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.ApplicationResult;
import system.search.results.GoogleSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GoogleSearchAgent extends AbstractAgent {
    private ApplicationManager applicationManager;

    public GoogleSearchAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle);

        this.applicationManager = applicationManager;
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        GoogleSearchResult result = new GoogleSearchResult(searchEngine, resourceBundle, query);
        ArrayList<GoogleSearchResult> results = new ArrayList<>(1);
        results.add(result);
        return results;
    }
}
