package system.search.results

import model.command.Command
import system.bookmarks.Bookmark
import system.context.SearchContext
import system.search.annotations.FilterableResult
import java.math.BigDecimal

@FilterableResult(filterName = "bookmark_category")
class BookmarkResult(context: SearchContext, val bookmark: Bookmark) : AbstractResult(context) {
    override val title: String
        get() = bookmark.title
    override val description: String?
        get() = bookmark.url

    override val imageId: String?
        get() = "asset:star"

    override fun executeAction() {
        context.applicationManager.openWebLink(bookmark.url)
    }

    override fun generateDragAndDropPayload(): String? = bookmark.url
}