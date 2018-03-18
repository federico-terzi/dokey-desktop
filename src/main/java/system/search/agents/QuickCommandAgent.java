package system.search.agents;

import system.quick_commands.QuickCommand;
import system.quick_commands.QuickCommandManager;
import system.model.ApplicationManager;
import system.quick_commands.model.DependencyResolver;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.BookmarkResult;
import system.search.results.QuickCommandResult;
import utils.ImageResolver;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuickCommandAgent extends AbstractAgent {
    private QuickCommandManager quickCommandManager;
    private DependencyResolver dependencyResolver;

    public QuickCommandAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, QuickCommandManager quickCommandManager,
                             DependencyResolver dependencyResolver) {
        super(searchEngine, resourceBundle);

        this.quickCommandManager = quickCommandManager;
        this.dependencyResolver = dependencyResolver;

        this.defaultImage = ImageResolver.getInstance().getImage(QuickCommandAgent.class.getResourceAsStream("/assets/star.png"), 32);
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        String lowerCaseQuery = query.toLowerCase();

        List<QuickCommandResult> results = quickCommandManager.searchCommands(query.toLowerCase(), MAX_RESULTS_FOR_AGENT)
                .stream().map((quickCommand -> {
            return new QuickCommandResult(searchEngine, resourceBundle, quickCommand, dependencyResolver, defaultImage);
        })).collect(Collectors.toList());
        return results;
    }
}
