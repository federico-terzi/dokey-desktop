
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
            long isActive = MACUtils.messageLong(nextObj, "isActive");

            // Make sure find the active one
            if (isActive == 1) { // NSApplicationActivationPolicyRegular
                // Get the app path
                Pointer executableURL = MACUtils.message(nextObj, "executableURL");
                Pointer pathPtr = MACUtils.message(executableURL, "path");
                Pointer utfPath = MACUtils.message(pathPtr, "UTF8String");
                String path = utfPath.getString(0);

                // Get the PID
                long pid = MACUtils.messageLong(nextObj, "processIdentifier");

                System.out.println(path);
                System.out.println(pid);
            }
        }
    }
}