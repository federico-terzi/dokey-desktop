package system.search.results

import system.context.SearchContext
import system.search.annotations.FilterableResult
import java.net.URLEncoder

@FilterableResult
class GoogleSearchResult(context: SearchContext, val query: String) : AbstractResult(context) {
    override val title: String
        get() = query
    override val description: String?
        get() = "${context.resourceBundle.getString("search")} $query ${context.resourceBundle.getString("on_google")}"

    override val imageId: String?
        get() = "asset:google"

    override val category = ResultCategory(context.resourceBundle.getString("google_search_category"), 30)

    override fun executeAction() {
        context.applicationManager.openWebLink("https://www.google.it/search?q=${URLEncoder.encode(query, "UTF-8")}")
    }
}