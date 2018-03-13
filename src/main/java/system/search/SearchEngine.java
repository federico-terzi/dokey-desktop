package system.search;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import system.model.ApplicationManager;
import system.search.agents.*;
import system.search.results.AbstractResult;
import system.search.results.CalculatorResult;
import system.search.results.GoogleSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible of passing the query to all agents and retrieving the results.
 */
public class SearchEngine implements ApplicationContextAware{
    public static final int MAX_RESULTS = 6; // Maximum number of results

    private ApplicationContext context;
    private ApplicationManager applicationManager;

    private List<AbstractAgent> agents = new ArrayList<>(10);

    public SearchEngine(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

        registerAgents();
    }

    /**
     * Register all the agents
     */
    private void registerAgents() {
        agents.add(context.getBean(ApplicationAgent.class));
        agents.add(context.getBean(CalculatorAgent.class));
        agents.add(context.getBean(TerminalAgent.class));
        agents.add(context.getBean(DebugAgent.class));
        agents.add(context.getBean(GoogleSearchAgent.class));
        agents.add(context.getBean(BookmarkAgent.class));
    }

    public void requestQuery(String query, OnQueryResultListener listener) {
        if (listener == null)  // Listener check
            return;

        // TODO: multithreaded

        new Thread(() -> {
            List<AbstractResult> results = new ArrayList<>();

            // Make sure the query is not empty
            if (!query.trim().isEmpty()) {
                // For each agent, request the results
                for (AbstractAgent agent : agents) {
                    if (agent.validate(query)) {
                        for (AbstractResult result : agent.getResults(query)) {
                            if (results.size() < MAX_RESULTS) {
                                results.add(result);
                                listener.onQueryResult(results);
                            }else{
                                break;
                            }
                        }
                    }
                }
            }else{
                listener.onQueryResult(results);  // For empty queries
            }
        }).start();
    }

    public interface OnQueryResultListener {
        void onQueryResult(List<AbstractResult> results);
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }
}
