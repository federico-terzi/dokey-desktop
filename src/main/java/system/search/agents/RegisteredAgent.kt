package system.search.agents

import system.applications.Application
import system.search.results.Result
import kotlin.reflect.KClass

/**
 * This class implements the Decorator pattern to add priority and result class attributes to the agent class
 * dynamically at runtime
 */
class RegisteredAgent(val agent: Agent, val priority: Int, val resultClass: KClass<out Result>) : Agent {
    override fun validate(query: String): Boolean {
        return agent.validate(query)
    }

    override fun getResults(query: String, activeApplication: Application?): List<out Result> {
        return agent.getResults(query, activeApplication)
    }
}