package system.applications.MAC;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;

/**
 * Binding to the native library used to manage active applications.
 */
public interface MacApplicationLib extends Library {
    MacApplicationLib INSTANCE = (MacApplicationLib) Native.loadLibrary("MacApplicationLib", MacApplicationLib.class);

    /*
     Focus the Dokey window
     */
    int focusDokey();

    /*
     Extract the icon of the specified app bundle to the given targetFile,
     using the PNG format.
    */
    int extractApplicationIcon(String appPath, String targetFile);

    /*
     Get the bundle path of the currently active application, copying it to the
     given pathBuffer.
     */
    void getActiveApplication(byte[] pathBuffer, int bufferSize);

    /*
     Get the bundle path of the currently active applications.
     For each active application, the callback will be invoked
     */
    void getActiveApplications(ActiveApplicationCallback callback);
    interface ActiveApplicationCallback extends Callback {
        void invoke(String appPath);
    }

    /*
     Return the PID of the currently active application.
     */
    int getActivePID();

    /*
     Activate ( focus ) the given application.
     If succeeded, return 1.
     If the application is not running, nothing will occur and the method will return 0.
     */
    int activateRunningApplication(String appPath);
}