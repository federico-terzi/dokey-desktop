package system.search.agents

import system.applications.Application
import system.search.results.Result

const val MAX_RESULTS_FOR_AGENT = 9

interface Agent {
    /**
     * This method is called before getResults() to make sure this agent
     * supports this query type.
     * If false is returned, the getResults() method will not be called.
     */
    fun validate(query: String) : Boolean

    /**
     * Get all the search results for the given query.
     */
    fun getResults(query: String, activeApplication: Application?) : List<out Result>
}