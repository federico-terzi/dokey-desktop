package system.search.agents

import com.udojava.evalex.Expression
import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.CommandResult
import system.search.results.Result
import system.search.results.CalculatorResult
import java.math.BigDecimal



@RegisterAgent(priority = 100, resultClass = CalculatorResult::class)
class CalculatorAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean {
        return query.matches(".*\\d+.*".toRegex());  // Check if query contains numbers
    }

    override fun getResults(query: String): List<out Result> {
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