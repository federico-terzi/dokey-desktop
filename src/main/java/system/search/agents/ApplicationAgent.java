package system.search.agents;

import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.ApplicationResult;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ApplicationAgent extends AbstractAgent {
    private ApplicationManager applicationManager;

    public ApplicationAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle, ApplicationResult.class);

        this.applicationManager = applicationManager;
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        String lowerCaseQuery = query.toLowerCase();

        List<ApplicationResult> results = applicationManager.getApplicationList().stream().filter(
                application -> application.getName().toLowerCase().contains(lowerCaseQuery))
                .limit(MAX_RESULTS_FOR_AGENT)
                .map((application -> new ApplicationResult(searchEngine, resourceBundle, application)))
                .collect(Collectors.toList());
        return results;
    }
}
