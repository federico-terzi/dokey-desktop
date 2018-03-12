package system.search.agents;

import system.DebugManager;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.DebugResult;
import system.search.results.TerminalResult;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DebugAgent extends AbstractAgent {
    private final DebugManager debugManager;

    public DebugAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, DebugManager debugManager) {
        super(searchEngine, resourceBundle);
        this.debugManager = debugManager;
    }

    @Override
    public boolean validate(String query) {
        return query.startsWith("#!");
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        DebugResult result = new DebugResult(searchEngine, resourceBundle, debugManager, query);
        ArrayList<DebugResult> results = new ArrayList<>(1);
        results.add(result);
        return results;
    }
}
