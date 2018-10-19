package system.search.agents

import com.udojava.evalex.Expression
import system.applications.Application
import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.Result
import system.search.results.CalculatorResult
import system.search.results.ConversionResult
import system.search.results.ConversionType


@RegisterAgent(priority = 100)
class CalculatorAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean {
        return query.matches(".*\\d+.*".toRegex());  // Check if query contains numbers
    }

    override fun getResults(query: String, activeApplication: Application?): List<out Result> {
        val results = mutableListOf<Result>()

        // Evaluate the expression
        val expression = Expression(query)
        try {
            val expressionResult = expression.eval()
            val result = CalculatorResult(context, query, expressionResult)
            results.add(result)

            // Calculate the conversions
            // Check if the result is an integer
            if (expressionResult.stripTrailingZeros().scale() <= 0) {
                val intResult = expressionResult.toInt()

                // Hex calculation
                val hexString = "0x${Integer.toHexString(intResult).toUpperCase().padStart(8, '0')}"
                val hexResult = ConversionResult(context, ConversionType.HEX, intResult.toString(), hexString)
                results.add(hexResult)

                // Binary calculation
                val binString = Integer.toBinaryString(intResult).toUpperCase().padStart(32, '0')
                val binPrettyString = "${binString.substring(0, 8)} ${binString.substring(8, 16)} ${binString.substring(16, 24)} ${binString.substring(24)}"
                val binResult = ConversionResult(context, ConversionType.BIN, intResult.toString(), binPrettyString)
                results.add(binResult)
            }
        }catch (e: Exception){}

        return results
    }
}