package system.search.agents;

import system.search.results.AbstractResult;

import java.util.List;

/**
 * Represents an abstract search agent, an entity responsible of returning specific results based
 * on the searched query.
 */
public abstract class AbstractAgent {
    /**
     * This method is called before getResults() to make sure this agent
     * supports this query type.
     * If false is returned, the getResults() method will not be called.
     * @param query the search query
     * @return true if query is supported, false otherwise.
     */
    public abstract boolean validate(String query);

    /**
     * Get all the search results for the given query.
     * @param query the search query.
     * @return a List of results.
     */
    public abstract List<? extends AbstractResult> getResults(String query);
}
