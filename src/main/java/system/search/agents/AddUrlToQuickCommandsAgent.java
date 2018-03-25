package system.search.agents;

import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.AddUrlToQuickCommandsResult;
import system.search.results.TerminalResult;
import utils.ImageResolver;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddUrlToQuickCommandsAgent extends AbstractAgent {
    public AddUrlToQuickCommandsAgent(SearchEngine searchEngine, ResourceBundle resourceBundle) {
        super(searchEngine, resourceBundle, AddUrlToQuickCommandsResult.class);

        this.defaultImage = ImageResolver.getInstance().getImage(AddUrlToQuickCommandsAgent.class.getResourceAsStream("/assets/right.png"), 32);
    }

    @Override
    public boolean validate(String query) {
        query = query.trim();

        if (!query.startsWith("http")) {
            return false;
        }

        // Make sure the url is valid
        try {
            URL u = new URL(query);
            u.toURI();

            return true;
        } catch (Exception e) {}

        return false;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        AddUrlToQuickCommandsResult result = new AddUrlToQuickCommandsResult(searchEngine, resourceBundle, query, defaultImage);
        ArrayList<AddUrlToQuickCommandsResult> results = new ArrayList<>(1);
        results.add(result);
        return results;
    }
}
