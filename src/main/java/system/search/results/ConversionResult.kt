package system.search.results

import model.command.Command
import system.context.SearchContext
import system.search.annotations.FilterableResult
import java.math.BigDecimal

enum class ConversionType(val imageId: String, val descriptionId: String) {
    HEX("asset:hex", "hex_conversion"),
    BIN("asset:bin", "bin_conversion")
}

class ConversionResult(context: SearchContext, val conversionType: ConversionType, val query : String,
                       val result: String) : AbstractResult(context) {
    override val title: String
        get() = result
    override val description: String?
        get() = "${context.resourceBundle.getString(conversionType.descriptionId)} $query"

    override val category = ResultCategory(context.resourceBundle.getString("calculator_category"), 100)

            override val imageId: String?
        get() = conversionType.imageId
}