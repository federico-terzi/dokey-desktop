package system.search;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import system.search.agents.AbstractAgent;
import system.search.agents.ApplicationAgent;
import system.search.results.AbstractResult;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible of passing the query to all agents and retrieving the results.
 */
public class SearchEngine implements ApplicationContextAware{
    private ApplicationContext context;

    private List<AbstractAgent> agents = new ArrayList<>(10);

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
    }

    public void requestQuery(String query, OnQueryResultListener listener) {
        if (listener == null)  // Listener check
            return;

        // TODO: multithreaded

        new Thread(() -> {
            List<AbstractResult> results = new ArrayList<>();

            for (AbstractAgent agent : agents) {
                if (agent.validate(query)) {
                    results.addAll(agent.getResults(query));
                }
            }

            listener.onQueryResult(results);
        }).start();
    }

    public interface OnQueryResultListener {
        void onQueryResult(List<AbstractResult> results);
    }
}
