package system.search.agents

import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.BookmarkResult
import system.search.results.CommandResult
import system.search.results.Result

@RegisterAgent(priority = 40, resultClass = BookmarkResult::class)
class BookmarkAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean = true

    override fun getResults(query: String): List<out Result> {
        val bookmarkResults = context.bookmarkManager.searchBookmarks(query.toLowerCase(), MAX_RESULTS_FOR_AGENT)
        return bookmarkResults.map {bookmark ->
            BookmarkResult(context, bookmark)
        }
    }
}