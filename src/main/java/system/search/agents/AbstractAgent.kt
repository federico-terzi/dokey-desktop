package system.search.agents

import system.context.SearchContext
import system.search.results.AbstractResult

abstract class AbstractAgent(val context: SearchContext) {
    /**
     * This method is called before getResults() to make sure this agent
     * supports this query type.
     * If false is returned, the getResults() method will not be called.
     */
    abstract fun validate(query: String) : Boolean

    /**
     * Get all the search results for the given query.
     */
    abstract fun getResults(query: String) : List<out AbstractResult>

    abstract val resultClass: Class<out AbstractResult>
}