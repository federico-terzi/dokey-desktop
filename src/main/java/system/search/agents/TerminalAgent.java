package system.search.agents;

import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.GoogleSearchResult;
import system.search.results.TerminalResult;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TerminalAgent extends AbstractAgent {
    private ApplicationManager applicationManager;

    public TerminalAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle);

        this.applicationManager = applicationManager;
    }

    @Override
    public boolean validate(String query) {
        return query.startsWith(">");
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        TerminalResult result = new TerminalResult(searchEngine, resourceBundle, query);
        ArrayList<TerminalResult> results = new ArrayList<>(1);
        results.add(result);
        return results;
    }
}
