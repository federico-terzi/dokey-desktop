package system.search.agents

import com.udojava.evalex.Expression
import system.applications.Application
import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.Result
import system.search.results.CalculatorResult


@RegisterAgent(priority = 100, resultClass = CalculatorResult::class)
class CalculatorAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean {
        return query.matches(".*\\d+.*".toRegex());  // Check if query contains numbers
    }

    override fun getResults(query: String, activeApplication: Application?): List<out Result> {
        // Evaluate the expression
        val expression = Expression(query)
        try {
            val expressionResult = expression.eval()
            val result = CalculatorResult(context, query, expressionResult)
            return listOf(result)
        }catch (e: Exception){}

        return emptyList()
    }
}