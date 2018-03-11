
import com.sun.jna.Pointer;
import system.MAC.MACUtils;
import system.model.Application;

import java.io.IOException;

public class Test2Main{

    public static void main(String[] args) throws IOException {
        Pointer nsWorkspace = MACUtils.lookUpClass("NSWorkspace");
        Pointer sharedWorkspace = MACUtils.message(nsWorkspace, "sharedWorkspace");
        Pointer runningApplications = MACUtils.message(sharedWorkspace, "runningApplications");

        // Get objects count
        long count = MACUtils.messageLong(runningApplications, "count");

        Pointer enumerator = MACUtils.message(runningApplications, "objectEnumerator");

        // Cycle through
        for (int i = 0; i<count; i++) {
            Pointer nextObj = MACUtils.message(enumerator, "nextObject");
            // Get the PID
            long pid = MACUtils.messageLong(nextObj, "processIdentifier");

            if (pid == 246) {
                MACUtils.message(nextObj, "activateWithOptions:", 2);
            }
        }
    }
}
