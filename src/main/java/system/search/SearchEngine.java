package system.search;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import system.model.ApplicationManager;
import system.search.agents.*;
import system.search.results.AbstractResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible of passing the query to all agents and retrieving the results.
 */
public class SearchEngine implements ApplicationContextAware{
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
        // The priority for the search agent is determined by its position in the list.
        // The first elements are processed before the last ones.

        agents.add(context.getBean(ApplicationAgent.class));
        agents.add(context.getBean(CalculatorAgent.class));
        agents.add(context.getBean(TerminalAgent.class));
        agents.add(context.getBean(QuickCommandAgent.class));
        agents.add(context.getBean(DebugAgent.class));
        agents.add(context.getBean(ShortcutAgent.class));
        agents.add(context.getBean(AddUrlToQuickCommandsAgent.class));
        agents.add(context.getBean(BookmarkAgent.class));
        agents.add(context.getBean(GoogleSearchAgent.class));
    }

    public void requestQuery(String query, OnQueryResultListener listener) {
        if (listener == null)  // Listener check
            return;

        new Thread(() -> {
            Map<Class<? extends AbstractResult>, List<? extends AbstractResult>> results = new HashMap<>();

            // Make sure the query is not empty
            if (!query.trim().isEmpty()) {
                // For each agent, request the results
                for (AbstractAgent agent : agents) {
                    if (agent.validate(query)) {
                        List<? extends AbstractResult> agentResults = agent.getResults(query);

                        if (agentResults.size() > 0) {
                            results.put(agent.getResultClass(), agentResults);
                            listener.onQueryResult(results);
                        }
                    }
                }
            }else{
                listener.onQueryResult(results);  // For empty queries
            }
        }).start();
    }

    public interface OnQueryResultListener {
        void onQueryResult(Map<Class<? extends AbstractResult>, List<? extends AbstractResult>> results);
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }
}
