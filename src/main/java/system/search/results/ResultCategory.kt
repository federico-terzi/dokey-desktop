package system.search.results

data class ResultCategory(val name: String, val priority: Int) : Comparable<ResultCategory> {
    override fun compareTo(other: ResultCategory): Int {
        return other.priority.compareTo(priority)  // Reversed order
    }
}