package system.search.results

import model.command.Command
import system.context.SearchContext
import system.search.annotations.FilterableResult
import java.math.BigDecimal

class CalculatorResult(context: SearchContext, val query : String, val result: BigDecimal) : AbstractResult(context) {
    override val title: String
        get() = "$query = $result"
    override val description: String?
        get() = "${context.resourceBundle.getString("result_of_expression")} $result"

    override val category = ResultCategory(context.resourceBundle.getString("calculator_category"), 100)

            override val imageId: String?
        get() = "asset:light"
}