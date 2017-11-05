package window

import javax.swing.Icon

data class Window(val PID: Int, val titleText : String, val icon : Icon,
                  val executablePath : String) {
}