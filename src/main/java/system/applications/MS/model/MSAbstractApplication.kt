package system.applications.MS.model

import com.sun.jna.platform.win32.WinDef
import system.applications.Application

abstract class MSAbstractApplication(id: String) : Application(id) {
    var lastKnownHandle : WinDef.HWND? = null
}