package system.search

import org.reflections.Reflections
import system.applications.Application
import system.context.SearchContext
import system.applications.ApplicationManager
import system.search.agents.Agent
import system.search.agents.RegisteredAgent
import system.search.annotations.RegisterAgent
import system.search.results.Result
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.reflect.KClass

/**
 * This class is responsible of passing the query to all agents and retrieving the results.
 */
class SearchEngine(val applicationManager: ApplicationManager, val context: SearchContext) {
    private val agents = mutableListOf<RegisteredAgent>()

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
        agentsClasses.forEach { agentClass ->
            val agentAnnotation = agentClass.getAnnotation(RegisterAgent::class.java) as RegisterAgent
            val agent = agentClass.getConstructor(SearchContext::class.java).newInstance(context) as Agent
            val registeredAgent = RegisteredAgent(agent, agentAnnotation.priority, agentAnnotation.resultClass)
            agents.add(registeredAgent)
        }
        // Reorder the agents based on priority
        agents.sortByDescending { it.priority }
    }

    fun requestQuery(query: String, activeApplication: Application?, listener: (query: String, category: KClass<out Result>, results: List<out Result>) -> Unit) {
        // Reset the executor queue with previous requests
        executor.queue.clear()

        for (agent in agents) {
            if (agent.validate(query)) {
                executor.execute({
                    val agentResults = agent.getResults(query, activeApplication)
                    if (agentResults.isNotEmpty()) {
                        listener(query, agent.resultClass, agentResults)
                    }
                })
            }
        }
    }
}
