package system.commands

data class CommandDescriptor(val title : String, val iconId: String) {
    override fun toString(): String {
        return title
    }
}