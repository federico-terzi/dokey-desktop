package system.search.agents;

import system.model.ApplicationManager;
import system.search.results.AbstractResult;
import system.search.results.ApplicationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationAgent extends AbstractAgent {
    private ApplicationManager applicationManager;

    public ApplicationAgent(ApplicationManager applicationManager) {
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
                .map(ApplicationResult::new)
                .collect(Collectors.toList());
        return results;
    }
}
