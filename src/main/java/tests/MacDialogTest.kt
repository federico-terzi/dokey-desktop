package tests

import com.sun.jna.Library
import com.sun.jna.Native
import system.applications.MAC.MACUtils

fun main(args: Array<String>) {
    /*val alertClass = MACUtils.lookUpClass("NSAlert")
    val allocAlert = MACUtils.message(alertClass, "alloc")
    val alert = MACUtils.message(allocAlert, "init")

    MACUtils.message(alert, "runModal")*/

    System.setProperty("jna.library.path", "/Users/freddy/Documents/TestLib")
    val dialogLibrary = Native.loadLibrary("Dialog", DialogLibrary::class.java)
    dialogLibrary.displayDialog()

    while (true) {}


    /*val runningApplications = MACUtils.message(sharedWorkspace, "runningApplications")

    // Get objects count
    val count = MACUtils.messageLong(runningApplications, "count")

    val enumerator = MACUtils.message(runningApplications, "objectEnumerator")

    // Cycle through
    for (i in 0 until count) {
        val nextObj = MACUtils.message(enumerator, "nextObject")
        // Get the PID
        val pid = MACUtils.messageLong(nextObj, "processIdentifier")

        if (pid == 246L) {
            MACUtils.message(nextObj, "activateWithOptions:", 2)
        }
    }*/
}

interface DialogLibrary : Library {
    fun displayDialog()
}