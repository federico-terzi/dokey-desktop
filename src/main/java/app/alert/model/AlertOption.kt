package app.alert.model

data class AlertOption(val text : String, val callback: (() -> Unit)? = null)