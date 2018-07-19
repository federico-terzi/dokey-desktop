package system.search

import org.reflections.Reflections
import org.springframework.context.ApplicationContext
import system.context.SearchContext
import system.model.ApplicationManager
import system.search.agents.*
import system.search.annotations.RegisterAgent
import system.search.results.AbstractResult

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 * This class is responsible of passing the query to all agents and retrieving the results.
 */
class SearchEngine(val applicationManager: ApplicationManager) {
    private var context: ApplicationContext? = null

    private val agents = mutableListOf<AbstractAgent>()

    private val executor : ThreadPoolExecutor = Executors.newFixedThreadPool(4) as ThreadPoolExecutor

    init {
        registerAgents()
    }

    /**
     * Register all the agents using reflection
     */
    private fun registerAgents() {
        // Load all the command handlers dynamically in order of priority
        val reflections = Reflections("system.search.agents")
        val agentsClasses = reflections.getTypesAnnotatedWith(RegisterAgent::class.java)
        val unorderedAgents = mutableListOf<Pair<AbstractAgent, Int>>()
        agentsClasses.forEach { agentClass ->
            val agentAnnotation = agentClass.getAnnotation(RegisterAgent::class.java) as RegisterAgent
            val agent = agentClass.getConstructor(SearchContext::class.java).newInstance()
            unorderedAgents.add(Pair<AbstractAgent, Int>(agent as AbstractAgent, agentAnnotation.priority))
        }
        // Reorder the agents based on priority and add them to the list
        unorderedAgents.sortBy { it.second }
        unorderedAgents.forEach { this.agents.add(it.first) }
    }

    fun requestQuery(query: String, listener: OnQueryResultListener) {
        // Reset the executor queue with previous requests
        executor.queue.clear()

        for (agent in agents) {
            if (agent.validate(query)) {
                executor.execute(Runnable {
                    val agentResults = agent.getResults(query)
                    if (agentResults.isNotEmpty()) {
                        listener.onResultUpdate(query, agent.resultClass, agentResults)
                    }
                })
            }
        }
    }

    interface OnQueryResultListener {
        fun onResultUpdate(query: String, category: Class<out AbstractResult>, results: List<out AbstractResult>)
    }
}
