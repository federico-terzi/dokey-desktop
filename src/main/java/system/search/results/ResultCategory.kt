package system.search.results

data class ResultCategory(val name: String, val priority: Int) : Comparable<ResultCategory> {
    override fun compareTo(other: ResultCategory): Int {
        if (other.priority != priority) {
            return other.priority.compareTo(priority)  // Reversed order
        }else{
            return name.compareTo(other.name)
        }
    }
}